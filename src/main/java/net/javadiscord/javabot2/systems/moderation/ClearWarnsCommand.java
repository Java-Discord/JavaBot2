package net.javadiscord.javabot2.systems.moderation;

import net.javadiscord.javabot2.command.ResponseException;
import net.javadiscord.javabot2.command.Responses;
import net.javadiscord.javabot2.command.SlashCommandHandler;
import org.javacord.api.interaction.SlashCommandInteraction;
import org.javacord.api.interaction.callback.InteractionImmediateResponseBuilder;

/**
 * A command which clears all warns from a user.
 */
public class ClearWarnsCommand implements SlashCommandHandler {
	@Override
	public InteractionImmediateResponseBuilder handle(SlashCommandInteraction interaction) throws ResponseException {
		var user = interaction.getOptionUserValueByName("user")
				.orElseThrow(ResponseException.warning("Missing user."));
		var moderationService = new ModerationService(interaction);
		moderationService.clearWarns(user, interaction.getUser());
		return Responses.successBuilder(interaction)
				.title("Warns Cleared")
				.messageFormat("Cleared all warns from %s.", user.getDiscriminatedName())
				.build();
	}
}
