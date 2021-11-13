package net.javadiscord.javabot2.command.interaction.button;

import net.javadiscord.javabot2.command.ResponseException;
import org.javacord.api.interaction.ButtonInteraction;
import org.javacord.api.interaction.callback.InteractionImmediateResponseBuilder;

/**
 * An interface that should be implemented by any class that is utilizing
 * button interactions.
 */
public interface ButtonHandler {
    InteractionImmediateResponseBuilder handleButtonInteraction(ButtonInteraction interaction) throws ResponseException;
}
