package net.javadiscord.javabot2.command.interaction.selection_menu;

import org.javacord.api.entity.message.component.SelectMenu;
import org.javacord.api.entity.message.component.SelectMenuBuilder;
import org.javacord.api.entity.message.component.SelectMenuOption;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Class that represents a single Select Menu Interaction.
 */
public class SelectMenuAction {

	private Class<? extends SelectionMenuHandler> handler;
	private List<SelectMenuOption> options;
	private String placeholder;
	private int minValue = 0;
	private int maxValue = 25;
	private boolean disabled = false;

	/**
	 * Constructor of the Select Menu Action.
	 */
	public SelectMenuAction() {
		this.options = new ArrayList<>();
	}

	/**
	 * Sets a handler class for this select menu interaction.
	 * The class must extend {@link SelectionMenuHandler}.
	 * @param handler a class that should handle the select menu interaction.
	 * @return the current instance in order to allow chain call methods.
	 */
	public SelectMenuAction handledBy(Class<? extends SelectionMenuHandler> handler) {
		this.handler = handler;
		return this;
	}

	/**
	 * Sets the select menu's placeholder text.
	 * @param placeholder the select menu's placeholder.
	 * @return the current instance in order to allow chain call methods.
	 */
	public SelectMenuAction setPlaceholder(String placeholder) {
		this.placeholder = placeholder;
		return this;
	}

	/**
	 * Enables/Disables the select menu based on the provided boolean.
	 * @param disabled the boolean which enables/disables the select menu.
	 * @return the current instance in order to allow chain call methods.
	 */
	public SelectMenuAction setDisabled(boolean disabled) {
		this.disabled = disabled;
		return this;
	}

	/**
	 * Sets the min value for select menu options.
	 * @param minValue the min value for select menu options.
	 * @return the current instance in order to allow chain call methods.
	 */
	public SelectMenuAction setMinValue(int minValue) {
		this.minValue = minValue;
		return this;
	}

	/**
	 * Sets the max value for select menu options.
	 * @param maxValue the max value for select menu options.
	 * @return the current instance in order to allow chain call methods.
	 */
	public SelectMenuAction setMaxValue(int maxValue) {
		this.maxValue = maxValue;
		return this;
	}

	/**
	 * Adds a single option to the select menu.
	 * @param option a single {@link SelectMenuOption}.
	 * @return the current instance in order to allow chain call methods.
	 */
	public SelectMenuAction addOption(SelectMenuOption option) {
		options.add(option);
		return this;
	}

	/**
	 * Adds multiple options to the select menu.
	 * @param options an array with all {@link SelectMenuOption}s.
	 * @return the current instance in order to allow chain call methods.
	 */
	public SelectMenuAction addOptions(SelectMenuOption... options) {
		this.options.addAll(Arrays.asList(options));
		return this;
	}

	/**
	 * Returns the select menu's custom id.
	 * (which is just the path of the handler class.)
	 * @return the custom id.
	 */
	public String getCustomId() {
		return handler.getName();
	}

	/**
	 * Returns the select menu's handler class.
	 * @return the select menu's handler class.
	 */
	public Class<? extends SelectionMenuHandler> getHandler() {
		return handler;
	}

	/**
	 * Returns a list with all options.
	 * @return a list with all options.
	 */
	public List<SelectMenuOption> getOptions() {
		return options;
	}

	/**
	 * Returns the complete select menu.
	 * @return the complete select menu.
	 */
	public SelectMenu getSelectMenu() {
		if (options.isEmpty()) throw new IllegalStateException("SelectMenu options may not be empty!");
		if (minValue > maxValue) throw new IllegalArgumentException("minValue may not be greater than maxValue!");
		var builder = new SelectMenuBuilder()
				.setCustomId(getCustomId())
				.setMinimumValues(minValue)
				.setMaximumValues(Math.min(maxValue, options.size())) // maxValue cannot be greater than the provided amount of options
				.addOptions(options)
				.setDisabled(disabled);
				if (placeholder != null && placeholder.length() > 0) builder.setPlaceholder(placeholder);
		return builder.build();
	}
}
