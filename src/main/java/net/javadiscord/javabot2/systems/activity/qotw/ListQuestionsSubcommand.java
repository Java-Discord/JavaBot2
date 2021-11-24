package net.javadiscord.javabot2.systems.activity.qotw;

import net.javadiscord.javabot2.command.ResponseException;
import net.javadiscord.javabot2.command.SlashCommandHandler;
import org.javacord.api.interaction.SlashCommandInteraction;
import org.javacord.api.interaction.callback.InteractionImmediateResponseBuilder;

public class ListQuestionsSubcommand implements SlashCommandHandler {
	/**
	 * Handles a slash command interaction.
	 *
	 * @param interaction The interaction.
	 * @return An immediate response to the interaction.
	 * @throws ResponseException If an error occurs while handling the event.
	 */
	@Override
	public InteractionImmediateResponseBuilder handle(SlashCommandInteraction interaction) throws ResponseException {
		return null;
	}
}
