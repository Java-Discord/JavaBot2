package net.javadiscord.javabot2.command.interaction.button;

import org.javacord.api.entity.emoji.Emoji;
import org.javacord.api.entity.message.component.Button;
import org.javacord.api.entity.message.component.ButtonBuilder;
import org.javacord.api.entity.message.component.ButtonStyle;

import java.util.ArrayList;
import java.util.List;

/**
 * Class that represents a single Button Interaction.
 */
public class ButtonAction {

    private final String label;
    private final ButtonStyle buttonStyle;
    private Class<? extends ButtonHandler> handler;
    private final List<String> params;
    private boolean disabled = false;
    private String url;
    private Emoji emoji;

    /**
     * Constructor of the Button Action.
     * @param label the button's label
     * @param buttonStyle the button's {@link ButtonStyle}
     */
    public ButtonAction(String label, ButtonStyle buttonStyle) {
        this.params = new ArrayList<>();
        this.label = label;
        this.buttonStyle = buttonStyle;
    }

    /**
     * Sets a handler class for this button interaction.
     * The class must extend {@link ButtonHandler}.
     * @param handler a class that should handle the button interaction.
     * @return the current instance in order to allow chain call methods.
     */
    public ButtonAction handledBy(Class<? extends ButtonHandler> handler) {
        this.handler = handler;
        return this;
    }

    /**
     * Adds a parameter, which is later baked into the button id.
     * @param param an object that is later converted to a string to bake it into the button id as a parameter.
     * @return the current instance in order to allow chain call methods.
     */
    public ButtonAction addParam(Object param) {
        params.add(String.valueOf(param));
        return this;
    }

    /**
     * Enables/Disables the button based on the provided boolean.
     * @param disabled the boolean which enables/disables the button.
     * @return the current instance in order to allow chain call methods.
     */
    public ButtonAction setDisabled(boolean disabled) {
        this.disabled = disabled;
        return this;
    }

    /**
     * Sets an Url for this button.
     * This only works for buttons with the {@link ButtonStyle#LINK} button style.
     * @param url the url as a string.
     * @return the current instance in order to allow chain call methods.
     */
    public ButtonAction setUrl(String url) {
        this.url = url;
        return this;
    }

    /**
     * Sets an Emoji for this button.
     * @param emoji the emoji which should be used.
     * @return the current instance in order to allow chain call methods.
     */
    public ButtonAction setEmoji(Emoji emoji) {
        this.emoji = emoji;
        return this;
    }

    /**
     * Returns the compiled button id. Every parameter is seperated with a colon.
     * The first argument is always the handler's class path.
     * After that, all set params ({@link ButtonAction#addParam(Object)}) get appended to the id.
     * @return the compiled button id.
     */
    public String getCustomId() {
        StringBuilder id = new StringBuilder(handler.getName());
        for (String param : params) {
            id.append(":").append(param);
        }
        return id.toString();
    }

    /**
     * Returns the button's {@link ButtonStyle}.
     * @return the button's {@link ButtonStyle}.
     */
    public ButtonStyle getButtonStyle() {
        return buttonStyle;
    }

    /**
     * Returns the button's label.
     * @return the button's label
     */
    public String getLabel() {
        return label;
    }

    /**
     * Returns the button's handler class.
     * @return The handler class
     */
    public Class<? extends ButtonHandler> getHandler() {
        return handler;
    }

    /**
     * Returns a list with all parameters of the button.
     * @return A List with all parameters.
     */
    public List<String> getParams() {
        return params;
    }

    /**
     * Returns the complete button.
     * @return the compiled button.
     */
    public Button getButton() {
        var builder = new ButtonBuilder()
                .setLabel(label)
                .setStyle(buttonStyle)
                .setDisabled(disabled);
        if (buttonStyle != ButtonStyle.LINK) builder.setCustomId(getCustomId());
        if (url != null && url.length() > 0 && buttonStyle == ButtonStyle.LINK) builder.setUrl(url);
        if (emoji != null) builder.setEmoji(emoji);

        return builder.build();
    }
}
