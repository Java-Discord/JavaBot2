package net.javadiscord.javabot2.command.interaction.button;

import org.javacord.api.entity.emoji.Emoji;
import org.javacord.api.entity.message.component.Button;
import org.javacord.api.entity.message.component.ButtonBuilder;
import org.javacord.api.entity.message.component.ButtonStyle;

import java.util.ArrayList;
import java.util.List;

public class ButtonAction {

    private final String label;
    private final ButtonStyle buttonStyle;
    private Class<? extends ButtonHandler> handler;
    private final List<String> params;
    private boolean disabled = false;
    private String url;
    private Emoji emoji;

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

    public ButtonAction setDisabled(boolean disabled) {
        this.disabled = disabled;
        return this;
    }

    public ButtonAction setUrl(String url) {
        this.url = url;
        return this;
    }

    public ButtonAction setEmoji(Emoji emoji) {
        this.emoji = emoji;
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

    public Button getButton() {
        var builder = new ButtonBuilder()
                .setLabel(label)
                .setStyle(buttonStyle)
                .setDisabled(disabled);
        if (buttonStyle != ButtonStyle.LINK) builder.setCustomId(getCustomId());
        if (url != null && !url.isBlank() && !url.isEmpty() && buttonStyle == ButtonStyle.LINK) builder.setUrl(url);
        if (emoji != null) builder.setEmoji(emoji);

        return builder.build();
    }
}
