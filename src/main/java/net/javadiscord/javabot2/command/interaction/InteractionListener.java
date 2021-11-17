package net.javadiscord.javabot2.command.interaction;

import net.javadiscord.javabot2.command.ResponseException;
import net.javadiscord.javabot2.command.interaction.button.ButtonHandler;
import net.javadiscord.javabot2.command.interaction.selection_menu.SelectionMenuHandler;
import org.javacord.api.event.interaction.ButtonClickEvent;
import org.javacord.api.event.interaction.SelectMenuChooseEvent;
import org.javacord.api.listener.interaction.ButtonClickListener;
import org.javacord.api.listener.interaction.SelectMenuChooseListener;

import java.lang.reflect.InvocationTargetException;

/**
 * Listener for all supported interactions.
 */
public class InteractionListener implements ButtonClickListener, SelectMenuChooseListener {

	@Override
	public void onButtonClick(ButtonClickEvent event) {
		var id = event.getButtonInteraction().getCustomId().split(":");
		ButtonHandler handler = (ButtonHandler) getHandlerByName(id[0]);
		try {
			handler.handleButtonInteraction(event.getButtonInteraction()).respond();
		} catch (ResponseException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onSelectMenuChoose(SelectMenuChooseEvent event) {
		var id = event.getSelectMenuInteraction().getCustomId().split(":");
		SelectionMenuHandler handler = (SelectionMenuHandler) getHandlerByName(id[0]);
		try {
			handler.handleSelectMenuInteraction(event.getSelectMenuInteraction()).respond();
		} catch (ResponseException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Tries to get a Class by the specified name
	 * (Class paths are baked into the button id) and returns it.
	 * @param name the class name
	 * @return The handler class
	 */
	private Object getHandlerByName(String name) {
		try {
			return Class.forName(name)
					.getDeclaredConstructor()
					.newInstance();
		} catch (ClassNotFoundException | NoSuchMethodException | InstantiationException |
				IllegalAccessException | InvocationTargetException e) {
			e.printStackTrace();
			return null;
		}
	}
}
