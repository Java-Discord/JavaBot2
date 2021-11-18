package net.javadiscord.javabot2.command;

import org.javacord.api.interaction.SlashCommandInteraction;
import org.javacord.api.interaction.callback.InteractionImmediateResponseBuilder;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Abstract command handler which is useful for commands which consist of lots
 * of subcommands. A child class will supply a map of subcommand handlers, so
 * that this parent handler can do the logic of finding the right subcommand to
 * invoke depending on the event received.
 */
public class DelegatingCommandHandler implements SlashCommandHandler {
	private final Map<String, SlashCommandHandler> subcommandHandlers;
	private final Map<String, SlashCommandHandler> subcommandGroupHandlers;

	/**
	 * Constructs the handler with an already-initialized map of subcommands.
	 * @param subcommandHandlers The map of subcommands to use.
	 */
	public DelegatingCommandHandler(Map<String, SlashCommandHandler> subcommandHandlers) {
		this.subcommandHandlers = subcommandHandlers;
		this.subcommandGroupHandlers = new HashMap<>();
	}

	/**
	 * Constructs the handler with an empty map, which subcommands can be added
	 * to via {@link DelegatingCommandHandler#addSubcommand(String, SlashCommandHandler)}.
	 */
	public DelegatingCommandHandler() {
		this.subcommandHandlers = new HashMap<>();
		this.subcommandGroupHandlers = new HashMap<>();
	}

	/**
	 * Gets an unmodifiable map of the subcommand handlers this delegating
	 * handler has registered.
	 * @return An unmodifiable map containing all registered subcommands.
	 */
	public Map<String, SlashCommandHandler> getSubcommandHandlers() {
		return Collections.unmodifiableMap(this.subcommandHandlers);
	}

	/**
	 * Gets an unmodifiable map of the subcommand group handlers that this
	 * handler has registered.
	 * @return An unmodifiable map containing all registered group handlers.
	 */
	public Map<String, SlashCommandHandler> getSubcommandGroupHandlers() {
		return Collections.unmodifiableMap(this.subcommandGroupHandlers);
	}

	/**
	 * Adds a subcommand to this handler.
	 * @param name The name of the subcommand. <em>This is case-sensitive.</em>
	 * @param handler The handler that will be called to handle subcommands with
	 *                the given name.
	 * @throws UnsupportedOperationException If this handler was initialized
	 * with an unmodifiable map of subcommand handlers.
	 */
	protected void addSubcommand(String name, SlashCommandHandler handler) {
		this.subcommandHandlers.put(name, handler);
	}

	/**
	 * Adds a subcommand group handler to this handler.
	 * @param name The name of the subcommand group. <em>This is case-sensitive.</em>
	 * @param handler The handler that will be called to handle commands within
	 *                the given subcommand's name.
	 * @throws UnsupportedOperationException If this handler was initialized
	 * with an unmodifiable map of subcommand group handlers.
	 */
	protected void addSubcommandGroup(String name, SlashCommandHandler handler) {
		this.subcommandGroupHandlers.put(name, handler);
	}

	/**
	 * Handles the case where the main command is called without any subcommand.
	 * @param interaction The event.
	 * @return The reply action that is sent to the user.
	 */
	protected InteractionImmediateResponseBuilder handleNonSubcommand(SlashCommandInteraction interaction) {
		return Responses.warning(interaction, "Missing Subcommand", "Please specify a subcommand.");
	}

	/**
	 * Handles a slash command interaction.
	 *
	 * @param interaction The interaction.
	 * @return An immediate response to the interaction.
	 * @throws ResponseException If an error occurs while handling the event.
	 */
	@Override
	public InteractionImmediateResponseBuilder handle(SlashCommandInteraction interaction) throws ResponseException {
		var subCommandOption = interaction.getOptionByIndex(0);
		if (subCommandOption.isPresent() && subCommandOption.get().isSubcommandOrGroup()) {
			var firstOption = subCommandOption.get();
			// TODO: Implement some way of handling subcommand groups! For now javacord is quite scuffed in that regard.
//			SlashCommandHandler groupHandler = this.getSubcommandGroupHandlers().get(firstOption.getName());
//			if (groupHandler != null) return groupHandler.handle(interaction);
			SlashCommandHandler subcommandHandler = this.getSubcommandHandlers().get(firstOption.getName());
			if (subcommandHandler != null) return subcommandHandler.handle(interaction);
		}
		return handleNonSubcommand(interaction);
	}
}
