package net.javadiscord.javabot2.command;

import org.javacord.api.DiscordApi;
import org.javacord.api.entity.message.MessageFlag;
import org.javacord.api.event.interaction.SlashCommandCreateEvent;
import org.javacord.api.interaction.SlashCommandBuilder;
import org.javacord.api.listener.interaction.SlashCommandCreateListener;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * This listener is responsible for handling any incoming slash commands sent by
 * users in servers where the bot is active, and responding to them by calling
 * the appropriate {@link SlashCommandHandler}.
 */
public final class SlashCommandListener implements SlashCommandCreateListener {
	private final Map<Long, SlashCommandHandler> commandHandlers = new HashMap<>();

	public SlashCommandListener(DiscordApi api) {
		registerSlashCommands(api, "commands/moderation.yaml")
				.thenAccept(commandHandlers::putAll);
	}

	@Override
	public void onSlashCommandCreate(SlashCommandCreateEvent event) {
		var handler = commandHandlers.get(event.getSlashCommandInteraction().getCommandId());
		if (handler != null) {
			try {
				handler.handle(event.getSlashCommandInteraction()).respond();
			} catch (Exception e) {
				e.printStackTrace();
				event.getSlashCommandInteraction().createImmediateResponder()
						.setFlags(MessageFlag.EPHEMERAL)
						.append("An error occurred and the command could not be executed.")
						.respond();
			}
		} else {
			event.getSlashCommandInteraction().createImmediateResponder()
					.setFlags(MessageFlag.EPHEMERAL)
					.append("There is no associated handler for this command.")
					.append("Please contact an administrator if this error persists.")
					.respond();
		}
	}

	private CompletableFuture<Map<Long, SlashCommandHandler>> registerSlashCommands(DiscordApi api, String... resources) {
		var commandConfigs = CommandDataLoader.load(resources);
		var handlers = initializeHandlers(commandConfigs);
		List<SlashCommandBuilder> commandBuilders = Arrays.stream(commandConfigs)
				.map(CommandConfig::toData).toList();
		return api.bulkOverwriteGlobalSlashCommands(commandBuilders)
				.thenApply(slashCommands -> {
					Map<Long, SlashCommandHandler> handlersById = new HashMap<>();
					for (var slashCommand : slashCommands) {
						var handler = handlers.get(slashCommand.getName());
						handlersById.put(slashCommand.getId(), handler);
					}
					// TODO: register permissions!
					return handlersById;
				});
	}

	private Map<String, SlashCommandHandler> initializeHandlers(CommandConfig[] commandConfigs) {
		Map<String, SlashCommandHandler> handlers = new HashMap<>();
		for (var commandConfig : commandConfigs) {
			if (commandConfig.getHandler() != null && !commandConfig.getHandler().isBlank()) {
				try {
					Class<?> handlerClass = Class.forName(commandConfig.getHandler());
					handlers.put(commandConfig.getName(), (SlashCommandHandler) handlerClass.getConstructor().newInstance());
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {
				System.err.println("Command " + commandConfig.getName() + " does not have an associated slash command.");
			}
		}
		return handlers;
	}
}
