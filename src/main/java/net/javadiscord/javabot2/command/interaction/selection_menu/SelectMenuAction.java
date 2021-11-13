package net.javadiscord.javabot2.command.interaction.selection_menu;

import org.javacord.api.entity.message.component.ActionRow;
import org.javacord.api.entity.message.component.SelectMenu;
import org.javacord.api.entity.message.component.SelectMenuOption;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SelectMenuAction {

    private Class<? extends SelectionMenuHandler> handler;
    private List<SelectMenuOption> options;
    private String placeholder = "Make a selection";
    private int minValue = 0;
    private int maxValue = 25;
    private boolean disabled = false;

    public SelectMenuAction() {
        this.options = new ArrayList<>();
    }

    public SelectMenuAction setMaxValue(int maxValue) {
        this.maxValue = maxValue;
        return this;
    }

    public SelectMenuAction setMinValue(int minValue) {
        this.minValue = minValue;
        return this;
    }

    public SelectMenuAction setPlaceholder(String placeholder) {
        this.placeholder = placeholder;
        return this;
    }

    public SelectMenuAction setDisabled(boolean disabled) {
        this.disabled = disabled;
        return this;
    }

    public SelectMenuAction handledBy(Class<? extends SelectionMenuHandler> handler) {
        this.handler = handler;
        return this;
    }

    public SelectMenuAction addOption(SelectMenuOption option) {
        options.add(option);
        return this;
    }

    public SelectMenuAction addOptions(SelectMenuOption... options) {
        this.options.addAll(Arrays.asList(options));
        return this;
    }

    public String getCustomId() {
        return handler.getName();
    }

    public Class<? extends SelectionMenuHandler> getHandler() { return handler; }

    public List<SelectMenuOption> getOptions() { return options; }

    public SelectMenu getSelectMenu() {
        if (options.isEmpty()) throw new IllegalStateException("SelectMenu options may not be empty!");
        if (minValue > maxValue) throw new IllegalArgumentException("minValue may not be greater than maxValue!");
        return SelectMenu.create(
                getCustomId(),
                placeholder,
                minValue,
                Math.min(maxValue, options.size()), // maxValue cannot be greater than the provided amount of options
                options,
                disabled
        );
    }
}
