package net.javadiscord.javabot2.command.interaction;

import net.javadiscord.javabot2.command.interaction.button.ButtonAction;
import net.javadiscord.javabot2.command.interaction.selection_menu.SelectMenuAction;
import org.javacord.api.entity.message.component.ActionRow;
import org.javacord.api.entity.message.component.LowLevelComponent;
import org.javacord.api.interaction.callback.InteractionImmediateResponseBuilder;

import java.util.ArrayList;
import java.util.List;

public class InteractionHandlerBuilder {

    private final InteractionImmediateResponseBuilder responseBuilder;
    private final List<LowLevelComponent> buttons = new ArrayList<>();
    private final List<LowLevelComponent> selectMenus = new ArrayList<>();

    public InteractionHandlerBuilder(InteractionImmediateResponseBuilder responseBuilder) {
        this.responseBuilder = responseBuilder;
    }

    public InteractionHandlerBuilder addButtons(ButtonAction... buttonActions) {
        for (var action : buttonActions) buttons.add(action.getButton());
        return this;
    }

    public InteractionHandlerBuilder addSelectionMenus(SelectMenuAction... selectMenuActions) {
        for (var action : selectMenuActions) {
            selectMenus.add(action.getSelectMenu());
        }
        return this;
    }

    public InteractionImmediateResponseBuilder getResponseBuilder() {
        return responseBuilder.addComponents(ActionRow.of(buttons), ActionRow.of(selectMenus));
    }
}
