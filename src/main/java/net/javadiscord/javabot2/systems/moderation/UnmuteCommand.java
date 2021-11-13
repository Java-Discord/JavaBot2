package net.javadiscord.javabot2.systems.moderation;

import net.javadiscord.javabot2.command.ResponseException;
import net.javadiscord.javabot2.command.Responses;
import net.javadiscord.javabot2.command.SlashCommandHandler;
import org.javacord.api.interaction.SlashCommandInteraction;
import org.javacord.api.interaction.callback.InteractionImmediateResponseBuilder;

/**
 * Command that unmutes a muted user.
 */
public class UnmuteCommand implements SlashCommandHandler {
	@Override
	public InteractionImmediateResponseBuilder handle(SlashCommandInteraction interaction) throws ResponseException {
		var user = interaction.getOptionUserValueByName("user")
				.orElseThrow(ResponseException.warning("Missing required user."));
		var moderationService = new ModerationService(interaction);
		moderationService.unmute(user, interaction.getUser());
		return Responses.successBuilder(interaction)
				.title("User Unmuted")
				.messageFormat("User %s has been unmuted.", user.getDiscriminatedName())
				.build();
	}
}
