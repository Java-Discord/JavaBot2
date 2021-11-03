package net.javadiscord.javabot2.command;

import org.javacord.api.interaction.SlashCommandInteraction;
import org.javacord.api.interaction.callback.InteractionImmediateResponseBuilder;

/**
 * An interface that should be implemented by any class that is defined as a
 * handler in any command configuration file.
 */
public interface SlashCommandHandler {
	/**
	 * Handles a slash command interaction.
	 * @param interaction The interaction.
	 * @return An immediate response to the interaction.
	 * @throws ResponseException If an error occurs while handling the event.
	 */
	InteractionImmediateResponseBuilder handle(SlashCommandInteraction interaction) throws ResponseException;
}
