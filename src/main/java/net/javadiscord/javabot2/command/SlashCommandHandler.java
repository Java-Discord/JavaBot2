package net.javadiscord.javabot2.command;

import org.javacord.api.interaction.SlashCommandInteraction;
import org.javacord.api.interaction.callback.InteractionImmediateResponseBuilder;

/**
 * An interface that should be implemented by any class that is defined as a
 * handler in any command configuration file.
 */
public interface SlashCommandHandler {
	InteractionImmediateResponseBuilder handle(SlashCommandInteraction interaction) throws Exception;
}
