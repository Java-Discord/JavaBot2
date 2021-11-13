package net.javadiscord.javabot2.command.interaction.button;

import org.javacord.api.entity.message.component.ActionRow;
import org.javacord.api.entity.message.component.Button;
import org.javacord.api.entity.message.component.ButtonStyle;

import java.util.ArrayList;
import java.util.List;

public class ButtonAction {

    private final String label;
    private final ButtonStyle buttonStyle;
    private Class<? extends ButtonHandler> handler;
    private final List<String> params;

    public ButtonAction(String label, ButtonStyle buttonStyle) {
        this.params = new ArrayList<>();

        this.label = label;
        this.buttonStyle = buttonStyle;
    }

    public ButtonAction handledBy(Class<? extends ButtonHandler> handler) {
        this.handler = handler;
        return this;
    }

    public ButtonAction addParam(Object param) {
        params.add(String.valueOf(param));
        return this;
    }

    public String getCustomId() {
        StringBuilder id = new StringBuilder(handler.getName());
        for (String param : params) {
            id.append(":").append(param);
        }
        return id.toString();
    }

    public ButtonStyle getButtonStyle() { return buttonStyle; }

    public String getLabel() { return label; }

    public Class<? extends ButtonHandler> getHandler() { return handler; }

    public List<String> getParams() { return params; }

    public Button getButton() { return Button.create(getCustomId(), buttonStyle, label); }
}
