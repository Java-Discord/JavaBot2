package net.javadiscord.javabot2.systems.moderation;

import net.javadiscord.javabot2.Bot;
import net.javadiscord.javabot2.command.ResponseException;
import net.javadiscord.javabot2.command.Responses;
import net.javadiscord.javabot2.command.SlashCommandHandler;
import net.javadiscord.javabot2.systems.moderation.model.WarnSeverity;
import org.javacord.api.interaction.SlashCommandInteraction;
import org.javacord.api.interaction.callback.InteractionImmediateResponseBuilder;

/**
 * Command that warns a user, which is used by moderators to enforce rules.
 */
public class WarnCommand implements SlashCommandHandler {
	@Override
	public InteractionImmediateResponseBuilder handle(SlashCommandInteraction interaction) throws ResponseException {
		var user = interaction.getOptionUserValueByName("user")
				.orElseThrow(ResponseException.warning("Missing required user."));
		var severityString = interaction.getOptionStringValueByName("severity")
				.orElseThrow(ResponseException.warning("Missing required severity."));
		var severity = WarnSeverity.valueOf(severityString.trim().toUpperCase());
		var reason = interaction.getOptionStringValueByName("reason")
				.orElseThrow(ResponseException.warning("Missing required reason."));
		var channel = interaction.getChannel()
				.orElseThrow(ResponseException.warning("Missing required channel."))
				.asServerTextChannel().orElseThrow(ResponseException.warning("This command can only be used in server text channels."));
		var moderationService = new ModerationService(interaction.getApi(), Bot.mongoDb, Bot.config.get(channel.getServer()).getModeration());
		moderationService.warn(user, severity, reason, interaction.getUser(), channel);
		return Responses.successBuilder(interaction)
				.title("User Warned")
				.messageFormat("User %s has been warned.", user.getMentionTag())
				.build();
	}
}
