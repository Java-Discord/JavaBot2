package net.javadiscord.javabot2.systems.moderation;


import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import net.javadiscord.javabot2.Bot;
import net.javadiscord.javabot2.config.guild.ModerationConfig;
import net.javadiscord.javabot2.systems.moderation.model.WarnSeverity;
import org.bson.Document;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.user.User;

import java.awt.*;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Map;

/**
 * This service provides methods for performing moderation actions, like banning
 * or warning users.
 */
public class ModerationService {
	private static final int BAN_DELETE_DAYS = 7;

	private final DiscordApi api;
	private final MongoDatabase db;
	private final ModerationConfig config;

	/**
	 * Constructs the service.
	 * @param api The API to use to interact with various discord entities.
	 * @param db The Mongo database.
	 * @param config The moderation config to use.
	 */
	public ModerationService(DiscordApi api, MongoDatabase db, ModerationConfig config) {
		this.api = api;
		this.db = db;
		this.config = config;
	}

	/**
	 * Issues a warning for the given user.
	 * @param user The user to warn.
	 * @param severity The severity of the warning.
	 * @param reason The reason for this warning.
	 * @param warnedBy The user who issued the warning.
	 * @param channel The channel in which the warning was issued.
	 */
	public void warn(User user, WarnSeverity severity, String reason, User warnedBy, ServerTextChannel channel) {
		var warns = db.getCollection("warn");
		Instant now = Instant.now();
		warns.insertOne(new Document(Map.of(
				"userId", user.getId(),
				"severity", severity.name(),
				"reason", reason,
				"warnedBy", warnedBy.getId(),
				"createdAt", now.toEpochMilli()
		)));
		int totalSeverity = 0;
		for (var warnDoc : findActiveWarns(user)) {
			totalSeverity += WarnSeverity.getWeightOrDefault(warnDoc.getString("severity"));
		}
		var warnEmbed = buildWarnEmbed(user, severity, reason, warnedBy, now, totalSeverity);
		Bot.asyncPool.submit(() -> {
			user.openPrivateChannel().thenAcceptAsync(privateChannel -> privateChannel.sendMessage(warnEmbed));
			channel.sendMessage(warnEmbed);
		});
		if (totalSeverity > config.getMaxWarnSeverity()) {
			ban(user, "Too many warnings.", warnedBy, channel);
		}
	}

	/**
	 * Bans a user.
	 * @param user The user to ban.
	 * @param reason The reason for banning the user.
	 * @param bannedBy The user who is responsible for banning this user.
	 * @param channel The channel in which the ban was issued.
	 */
	public void ban(User user, String reason, User bannedBy, ServerTextChannel channel) {
		var banEmbed = buildBanEmbed(user, reason, bannedBy);
		Bot.asyncPool.submit(() -> {
			channel.sendMessage(banEmbed);
			user.openPrivateChannel()
					.thenComposeAsync(privateChannel -> privateChannel.sendMessage(banEmbed))
					.thenRunAsync(() -> channel.getServer().banUser(user, BAN_DELETE_DAYS, reason));
		});
	}

	private FindIterable<Document> findActiveWarns(User user) {
		long warnTimeout = OffsetDateTime.now().minusDays(config.getWarnTimeoutDays())
				.toInstant().toEpochMilli();
		return db.getCollection("warn")
				.find(Filters.and(
						Filters.eq("userId", user.getId()),
						Filters.gt("createdAt", warnTimeout)
				));
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

	private EmbedBuilder buildBanEmbed(User user, String reason, User bannedBy) {
		return new EmbedBuilder()
				.setColor(Color.RED)
				.setTitle(String.format("%s | Ban", user.getDiscriminatedName()))
				.addField("Reason", reason)
				.setTimestampToNow()
				.setFooter(bannedBy.getDiscriminatedName(), bannedBy.getAvatar());
	}
}
