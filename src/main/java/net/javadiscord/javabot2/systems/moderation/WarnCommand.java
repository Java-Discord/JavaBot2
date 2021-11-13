package net.javadiscord.javabot2.systems.moderation;

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
		if (user.isBot()) return Responses.warning(interaction, "Cannot warn bots.");
		var severityString = interaction.getOptionStringValueByName("severity")
				.orElseThrow(ResponseException.warning("Missing required severity."));
		var severity = WarnSeverity.valueOf(severityString.trim().toUpperCase());
		var reason = interaction.getOptionStringValueByName("reason")
				.orElseThrow(ResponseException.warning("Missing required reason."));
		var channel = interaction.getChannel()
				.orElseThrow(ResponseException.warning("Missing required channel."))
				.asServerTextChannel().orElseThrow(ResponseException.warning("This command can only be used in server text channels."));
		var quiet = interaction.getOptionBooleanValueByName("quiet").orElse(false);
		if (user.isBot()) return Responses.warning(interaction, "Cannot warn a bot.");
		var moderationService = new ModerationService(interaction);
		moderationService.warn(user, severity, reason, interaction.getUser(), channel, quiet);
		return Responses.successBuilder(interaction)
				.title("User Warned")
				.messageFormat("User %s has been warned.", user.getMentionTag())
				.build();
	}
}
