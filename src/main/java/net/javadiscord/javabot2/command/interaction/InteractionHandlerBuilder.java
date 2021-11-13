package net.javadiscord.javabot2.command.interaction;

import net.javadiscord.javabot2.command.interaction.button.ButtonAction;
import net.javadiscord.javabot2.command.interaction.selection_menu.SelectMenuAction;
import org.javacord.api.entity.message.component.ActionRow;
import org.javacord.api.entity.message.component.LowLevelComponent;
import org.javacord.api.interaction.callback.InteractionImmediateResponseBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * Class that is used to build message interactions.
 */
public class InteractionHandlerBuilder {
    private final InteractionImmediateResponseBuilder responseBuilder;
    private final List<LowLevelComponent> buttons = new ArrayList<>();
    private final List<LowLevelComponent> selectMenus = new ArrayList<>();

    public InteractionHandlerBuilder(InteractionImmediateResponseBuilder responseBuilder) {
        this.responseBuilder = responseBuilder;
    }

    /**
     * Adds one or multiple {@link ButtonAction}s.
     * @param buttonActions an array of {@link ButtonAction}s.
     * @return the current instance in order to allow chain call methods.
     */
    public InteractionHandlerBuilder addButtons(ButtonAction... buttonActions) {
        for (var action : buttonActions) buttons.add(action.getButton());
        return this;
    }

    /**
     * Adds one or multiple {@link SelectMenuAction}s.
     * @param selectMenuActions an array of {@link SelectMenuAction}s.
     * @return the current instance in order to allow chain call methods.
     */
    public InteractionHandlerBuilder addSelectionMenus(SelectMenuAction... selectMenuActions) {
        for (var action : selectMenuActions) {
            selectMenus.add(action.getSelectMenu());
        }
        return this;
    }

    /**
     * Returns the provided Response Builder.
     * @return the current instance in order to allow chain call methods.
     */
    public InteractionImmediateResponseBuilder getResponseBuilder() {
        return responseBuilder.addComponents(ActionRow.of(buttons), ActionRow.of(selectMenus));
    }
}
