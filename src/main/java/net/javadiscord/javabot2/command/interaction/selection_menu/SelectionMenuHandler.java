package net.javadiscord.javabot2.command.interaction.selection_menu;

import net.javadiscord.javabot2.command.ResponseException;
import org.javacord.api.interaction.SelectMenuInteraction;
import org.javacord.api.interaction.callback.InteractionImmediateResponseBuilder;

/**
 * An interface that should be implemented by any class that is utilizing
 * select menu interactions.
 */
public interface SelectionMenuHandler {
    InteractionImmediateResponseBuilder handleSelectMenuInteraction(SelectMenuInteraction interaction) throws ResponseException;
}
