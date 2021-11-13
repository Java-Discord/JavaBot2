package net.javadiscord.javabot2.command.interaction.button;

import net.javadiscord.javabot2.command.ResponseException;
import org.javacord.api.interaction.ButtonInteraction;
import org.javacord.api.interaction.callback.InteractionImmediateResponseBuilder;

public interface ButtonHandler {
    InteractionImmediateResponseBuilder handleButtonInteraction(ButtonInteraction interaction) throws ResponseException;
}
