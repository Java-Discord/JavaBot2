package net.javadiscord.javabot2.systems.moderation;


import lombok.extern.slf4j.Slf4j;
import net.javadiscord.javabot2.Bot;
import net.javadiscord.javabot2.command.ResponseException;
import net.javadiscord.javabot2.config.guild.ModerationConfig;
import net.javadiscord.javabot2.db.DbActions;
import net.javadiscord.javabot2.systems.moderation.dao.MuteRepository;
import net.javadiscord.javabot2.systems.moderation.dao.WarnRepository;
import net.javadiscord.javabot2.systems.moderation.model.Mute;
import net.javadiscord.javabot2.systems.moderation.model.Warn;
import net.javadiscord.javabot2.systems.moderation.model.WarnSeverity;
import net.javadiscord.javabot2.util.TimeUtils;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.server.ServerUpdater;
import org.javacord.api.entity.user.User;
import org.javacord.api.interaction.SlashCommandInteraction;

import java.awt.*;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.concurrent.CompletableFuture;

/**
 * This service provides methods for performing moderation actions, like banning
 * or warning users.
 */
@Slf4j
public class ModerationService {
	private static final int BAN_DELETE_DAYS = 7;

	private final DiscordApi api;
	private final ModerationConfig config;

	/**
	 * Constructs the service.
	 * @param api The API to use to interact with various discord entities.
	 * @param config The moderation config to use.
	 */
	public ModerationService(DiscordApi api, ModerationConfig config) {
		this.api = api;
		this.config = config;
	}

	/**
	 * Constructs the service using information obtained from an interaction.
	 * @param interaction The interaction to use.
	 * @throws ResponseException If the interaction did not occur in the context
	 * of a server.
	 */
	public ModerationService(SlashCommandInteraction interaction) throws ResponseException {
		this(
				interaction.getApi(),
				Bot.config.get(interaction.getServer()
						.orElseThrow(ResponseException.warning("The moderation service can only be used in servers.")))
						.getModeration()
		);
	}

	/**
	 * Issues a warning for the given user.
	 * @param user The user to warn.
	 * @param severity The severity of the warning.
	 * @param reason The reason for this warning.
	 * @param warnedBy The user who issued the warning.
	 * @param channel The channel in which the warning was issued.
	 * @param quiet If true, don't send a message in the channel.
	 * @return A future that completes when all warn operations are complete.
	 */
	public CompletableFuture<Void> warn(User user, WarnSeverity severity, String reason, User warnedBy, ServerTextChannel channel, boolean quiet) {
		return DbActions.doDaoAction(WarnRepository::new, repo -> {
			var warn = repo.insert(new Warn(user.getId(), warnedBy.getId(), severity, reason));
			LocalDateTime cutoff = LocalDateTime.now().minusDays(config.getWarnTimeoutDays());
			int totalWeight = repo.getTotalSeverityWeight(user.getId(), cutoff);
			var warnEmbed = buildWarnEmbed(user, severity, reason, warnedBy, warn.getCreatedAt().toInstant(ZoneOffset.UTC), totalWeight);
			user.openPrivateChannel().thenAcceptAsync(pc -> pc.sendMessage(warnEmbed));
			config.getLogChannel().sendMessage(warnEmbed);
			if (!quiet && channel.getId() != config.getLogChannelId()) {
				channel.sendMessage(warnEmbed);
			}
			if (totalWeight > config.getMaxWarnSeverity()) {
				ban(user, "Too many warnings.", warnedBy, channel, quiet);
			}
		});
	}

	/**
	 * Clears warns from the given user by discarding all warns.
	 * @param user The user to clear warns from.
	 * @param clearedBy The user who cleared the warns.
	 * @return A future that completes when the warns have been cleared.
	 */
	public CompletableFuture<Void> clearWarns(User user, User clearedBy) {
		return DbActions.doDaoAction(WarnRepository::new, dao -> {
			dao.discardAll(user.getId());
			var embed = buildClearWarnsEmbed(user, clearedBy);
			user.openPrivateChannel().thenAcceptAsync(pc -> pc.sendMessage(embed));
			config.getLogChannel().sendMessage(embed);
		});
	}

	/**
	 * Bans a user.
	 * @param user The user to ban.
	 * @param reason The reason for banning the user.
	 * @param bannedBy The user who is responsible for banning this user.
	 * @param channel The channel in which the ban was issued.
	 * @param quiet If true, don't send a message in the channel.
	 * @return A future that completes once all ban operations are done.
	 */
	public CompletableFuture<Void> ban(User user, String reason, User bannedBy, ServerTextChannel channel, boolean quiet) {
		var banEmbed = buildBanEmbed(user, reason, bannedBy);
		if (channel.getServer().canBanUser(bannedBy, user)) {
			var future = channel.getServer().banUser(user, BAN_DELETE_DAYS, reason)
					.thenComposeAsync(unused -> user.openPrivateChannel())
					.thenComposeAsync(privateChannel -> privateChannel.sendMessage(banEmbed));
			if (!quiet) {
				future = future.thenComposeAsync(unused -> channel.sendMessage(banEmbed));
			}
			return future.thenApply(msg -> null);
		} else {
			return CompletableFuture.failedFuture(new PermissionException("You don't have permission to ban this user."));
		}
	}

	/**
	 * Mutes a user for a certain duration. If the user is already muted for an
	 * existing duration, this adds another sanction to their list so that they
	 * won't be unmuted until all mutes are cleared.
	 * @param user The user to mute.
	 * @param reason The reason for muting the user.
	 * @param mutedBy The user who is responsible for muting this user.
	 * @param duration The duration to be muted for.
	 * @param channel The channel in which the mute was issued.
	 * @param quiet If true, don't send a message in the channel.
	 * @return A future that completes when muting is done.
	 */
	public CompletableFuture<Void> mute(User user, String reason, User mutedBy, Duration duration, ServerTextChannel channel, boolean quiet) {
		return DbActions.doAction(con -> {
			con.setAutoCommit(false);
			var repo = new MuteRepository(con);
			var embed = buildMuteEmbed(user, reason, duration, mutedBy);
			// If the user doesn't currently have the mute role, give it to them and issue a new mute.
			if (!user.getRoles(channel.getServer()).contains(config.getMuteRole())) {
				// Discard any (erroneous) active mute for this user, if they exist.
				for (var activeMute : repo.getActiveMutes(user.getId())) {
					repo.discard(activeMute);
				}
				user.addRole(config.getMuteRole(), reason);
				repo.insert(new Mute(user.getId(), mutedBy.getId(), reason, LocalDateTime.now().plus(duration)));
			} else {
				// The user already has the mute role, so we should add a mute to extend the duration of any current mute.
				var activeMutes = repo.getActiveMutes(user.getId());
				Mute lastActiveMute = null;
				for (var activeMute : activeMutes) {
					if (lastActiveMute == null || activeMute.getEndsAt().isAfter(lastActiveMute.getEndsAt())) {
						lastActiveMute = activeMute;
					}
				}
				if (lastActiveMute == null) {
					// If the user is already muted, there should generally always be an active mute, except in edge cases near the ending time of a mute.
					// If there's no mute, just make a new one.
					repo.insert(new Mute(user.getId(), mutedBy.getId(), reason, LocalDateTime.now().plus(duration)));
				} else {
					// If there is a last active mute, add a new one that extends beyond its end time.
					repo.insert(new Mute(user.getId(), mutedBy.getId(), reason, lastActiveMute.getEndsAt().plus(duration)));
					// Discard all other active mutes, since they'll have no effect anymore.
					activeMutes.remove(lastActiveMute);
					for (var mute : activeMutes) {
						repo.discard(mute);
					}
				}
			}
			con.commit();
			user.openPrivateChannel().thenAcceptAsync(pc -> pc.sendMessage(embed));
			config.getLogChannel().sendMessage(embed);
			if (!quiet && channel.getId() != config.getLogChannelId()) {
				channel.sendMessage(embed);
			}
		});
	}

	/**
	 * Unmutes the given user.
	 * @param user The user to unmute.
	 * @param unmutedBy The user who unmuted the user.
	 * @return A future that completes when the user is unmuted.
	 */
	public CompletableFuture<Void> unmute(User user, User unmutedBy) {
		return DbActions.doAction(con -> {
			var repo = new MuteRepository(con);
			repo.discardAllActive(user.getId());
			user.removeRole(config.getMuteRole());
			var embed = buildUnmuteEmbed(user, unmutedBy);
			config.getLogChannel().sendMessage(embed);
			user.openPrivateChannel().thenAcceptAsync(pc -> pc.sendMessage(embed));
		});
	}

	/**
	 * Unmutes all users whose mutes have expired, and discards those mutes.
	 * @return A future that completes when all expired mutes have been processed.
	 */
	public CompletableFuture<Void> unmuteExpired() {
		return DbActions.doAction(con -> {
			con.setAutoCommit(false);
			var repo = new MuteRepository(con);
			ServerUpdater updater = new ServerUpdater(config.getGuild());
			for (var mute : repo.getExpiredMutes()) {
				// Check that for this expired mute, that there are no other active mutes which still apply to the user.
				if (!repo.hasActiveMutes(mute.getUserId())) {
					var user = api.getUserById(mute.getUserId()).join();
					if (user.getRoles(config.getGuild()).contains(config.getMuteRole())) {
						log.info("Unmuting user {} because their mute has expired.", user.getDiscriminatedName());
						updater.removeRoleFromUser(user, config.getMuteRole());
						var embed = buildUnmuteEmbed(user, api.getYourself());
						user.openPrivateChannel().thenAcceptAsync(pc -> pc.sendMessage(embed));
						config.getLogChannel().sendMessage(embed);
					}
					repo.discard(mute);
				}
			}
			updater.update();
			con.commit();
		});
	}

	private EmbedBuilder buildWarnEmbed(User user, WarnSeverity severity, String reason, User warnedBy, Instant timestamp, int totalSeverity) {
		return new EmbedBuilder()
				.setColor(Color.ORANGE)
				.setTitle(String.format("%s | Warn (%d/%d)", user.getDiscriminatedName(), totalSeverity, config.getMaxWarnSeverity()))
				.addField("Reason", reason)
				.setTimestamp(timestamp)
				.addField("Severity", severity.name())
				.setFooter(warnedBy.getDiscriminatedName(), warnedBy.getAvatar());
	}

	private EmbedBuilder buildClearWarnsEmbed(User user, User clearedBy) {
		return new EmbedBuilder()
				.setColor(Color.ORANGE)
				.setTitle(String.format("%s | Warns Cleared", user.getDiscriminatedName()))
				.setDescription("All warns have been cleared from " + user.getDiscriminatedName() + "'s record.")
				.setTimestampToNow()
				.setFooter(clearedBy.getDiscriminatedName(), clearedBy.getAvatar());
	}

	private EmbedBuilder buildBanEmbed(User user, String reason, User bannedBy) {
		return new EmbedBuilder()
				.setColor(Color.RED)
				.setTitle(String.format("%s | Ban", user.getDiscriminatedName()))
				.addField("Reason", reason)
				.setTimestampToNow()
				.setFooter(bannedBy.getDiscriminatedName(), bannedBy.getAvatar());
	}

	private EmbedBuilder buildMuteEmbed(User user, String reason, Duration duration, User mutedBy) {
		return new EmbedBuilder()
				.setColor(Color.DARK_GRAY)
				.setTitle(String.format("%s | Mute", user.getDiscriminatedName()))
				.addField("Reason", reason)
				.addField("Duration", TimeUtils.formatDuration(duration))
				.setTimestampToNow()
				.setFooter(mutedBy.getDiscriminatedName(), mutedBy.getAvatar());
	}

	private EmbedBuilder buildUnmuteEmbed(User user, User unmutedBy) {
		return new EmbedBuilder()
				.setColor(Color.DARK_GRAY)
				.setTitle(String.format("%s | Unmute", user.getDiscriminatedName()))
				.setTimestampToNow()
				.setFooter(unmutedBy.getDiscriminatedName(), unmutedBy.getAvatar());
	}
}
