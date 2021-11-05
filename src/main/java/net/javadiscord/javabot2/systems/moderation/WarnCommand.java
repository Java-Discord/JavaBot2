package net.javadiscord.javabot2.systems.moderation;

import com.mongodb.client.model.Filters;
import net.javadiscord.javabot2.Bot;
import net.javadiscord.javabot2.command.ResponseException;
import net.javadiscord.javabot2.command.Responses;
import net.javadiscord.javabot2.command.SlashCommandHandler;
import org.bson.Document;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.interaction.SlashCommandInteraction;
import org.javacord.api.interaction.callback.InteractionImmediateResponseBuilder;

import java.awt.*;
import java.time.Instant;
import java.util.Map;

/**
 * Command that warns a user, which is used by moderators to enforce rules.
 */
public class WarnCommand implements SlashCommandHandler {
	/**
	 * The maximum severity that a user can reach, after which they are banned.
	 */
	public static final int MAX_SEVERITY = 100;

	@Override
	public InteractionImmediateResponseBuilder handle(SlashCommandInteraction interaction) throws ResponseException {
		var server = interaction.getServer().orElseThrow(ResponseException.warning("This command can only be used in a server."));
		var user = interaction.getOptionUserValueByName("user")
				.orElseThrow(ResponseException.warning("Missing required user."));
		var severityString = interaction.getOptionStringValueByName("severity")
				.orElseThrow(ResponseException.warning("Missing required severity."));
		var severity = Severity.valueOf(severityString.trim().toUpperCase());
		var reason = interaction.getOptionStringValueByName("reason")
				.orElseThrow(ResponseException.warning("Missing required reason."));
		var warns = Bot.mongoDb.getCollection("warn");
		long timestamp = System.currentTimeMillis();
		warns.insertOne(new Document(Map.of(
				"userId", user.getId(),
				"severity", severity.name(),
				"reason", reason,
				"warnedBy", interaction.getUser().getId(),
				"createdAt", timestamp
		)));
		int totalSeverity = 0;
		for (var doc : warns.find(Filters.eq("userId", user.getId()))) {
			totalSeverity += Severity.valueOf(doc.getString("severity")).getWeight();
		}
		int finalTotalSeverity = totalSeverity;
		if (finalTotalSeverity > MAX_SEVERITY) {
			return banUser(interaction, user, server);
		} else {
			return warnUser(interaction, new WarnData(user, timestamp, severity, reason, interaction.getUser(), finalTotalSeverity, server));
		}
	}

	private InteractionImmediateResponseBuilder warnUser(SlashCommandInteraction interaction, WarnData warn) {
		EmbedBuilder embed = buildWarnEmbed(warn);
		Bot.asyncPool.submit(() -> {
			warn.user().openPrivateChannel().thenAcceptAsync(privateChannel -> privateChannel.sendMessage(embed));
			interaction.getChannel().orElseThrow().sendMessage(embed);
		});
		return Responses.successBuilder(interaction)
				.title("User Warned")
				.message(String.format("User %s has been warned.", warn.user().getMentionTag()))
				.build();
	}

	private EmbedBuilder buildWarnEmbed(WarnData warn) {
		return new EmbedBuilder()
				.setColor(Color.ORANGE)
				.setTitle(String.format("%s | Warn (%d/%d)", warn.user().getDisplayName(warn.server()), warn.totalSeverity(), MAX_SEVERITY))
				.addField("Reason", warn.reason())
				.setTimestamp(Instant.ofEpochMilli(warn.timestamp()))
				.addField("Severity", warn.severity().name())
				.setFooter(warn.warnedBy().getDisplayName(warn.server()));
	}

	private InteractionImmediateResponseBuilder banUser(SlashCommandInteraction interaction, User user, Server server) {
		user.openPrivateChannel().thenAcceptAsync(privateChannel -> privateChannel.sendMessage("You have been banned after receiving too many warnings."));
		server.banUser(user, 0, "Too many warnings.");
		return Responses.successBuilder(interaction)
				.title("User Banned")
				.message(String.format("User %s was banned after receiving too many warnings.", user.getDisplayName(server)))
				.build();
	}

	private enum Severity {
		LOW(10), MEDIUM(20), HIGH(40);

		private final int weight;

		Severity(int weight) {
			this.weight = weight;
		}

		public int getWeight() {
			return this.weight;
		}
	}

	private static record WarnData(User user, long timestamp, Severity severity, String reason, User warnedBy, int totalSeverity, Server server){}
}
