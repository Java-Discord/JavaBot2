package net.javadiscord.javabot2.command;

import lombok.extern.slf4j.Slf4j;
import net.javadiscord.javabot2.command.data.CommandConfig;
import net.javadiscord.javabot2.command.data.CommandDataLoader;
import org.javacord.api.DiscordApi;
import org.javacord.api.event.interaction.SlashCommandCreateEvent;
import org.javacord.api.interaction.ServerSlashCommandPermissionsBuilder;
import org.javacord.api.interaction.SlashCommandBuilder;
import org.javacord.api.interaction.SlashCommandPermissions;
import org.javacord.api.listener.interaction.SlashCommandCreateListener;

import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * This listener is responsible for handling any incoming slash commands sent by
 * users in servers where the bot is active, and responding to them by calling
 * the appropriate {@link SlashCommandHandler}.
 */
@Slf4j
public final class SlashCommandListener implements SlashCommandCreateListener {
	private final Map<Long, SlashCommandHandler> commandHandlers = new HashMap<>();

	/**
	 * Constructs a new slash command listener using the given Discord api, and
	 * loads commands from configuration YAML files according to the list of
	 * resources.
	 * @param api The Discord api to use.
	 * @param resources The list of classpath resources to load commands from.
	 */
	public SlashCommandListener(DiscordApi api, String... resources) {
		registerSlashCommands(api, resources)
				.thenAcceptAsync(commandHandlers::putAll);
	}

	@Override
	public void onSlashCommandCreate(SlashCommandCreateEvent event) {
		var handler = commandHandlers.get(event.getSlashCommandInteraction().getCommandId());
		if (handler != null) {
			try {
				handler.handle(event.getSlashCommandInteraction()).respond();
			} catch (ResponseException e) {
				e.getResponseBuilder().respond();
			} catch (Exception e) {
				log.error("An error occurred while handling a slash command.", e);
				Responses.errorBuilder(event)
						.message("An error occurred while executing the command.")
						.respond();
			}
		} else {
			Responses.warningBuilder(event)
					.title("No Handler")
					.message("There is no associated handler for this command. Please contact an administrator if this error persists.")
					.respond();
		}
	}

	private CompletableFuture<Map<Long, SlashCommandHandler>> registerSlashCommands(DiscordApi api, String... resources) {
		var commandConfigs = CommandDataLoader.load(resources);
		var handlers = initializeHandlers(commandConfigs);
		List<SlashCommandBuilder> commandBuilders = Arrays.stream(commandConfigs)
				.map(CommandConfig::toData).toList();
		return deleteAllSlashCommands(api)
				.thenComposeAsync(unused -> api.bulkOverwriteGlobalSlashCommands(commandBuilders))
				.thenComposeAsync(slashCommands -> {
					Map<Long, SlashCommandHandler> handlersById = new HashMap<>();
					Map<String, Long> nameToId = new HashMap<>();
					for (var slashCommand : slashCommands) {
						var handler = handlers.get(slashCommand.getName());
						handlersById.put(slashCommand.getId(), handler);
						nameToId.put(slashCommand.getName(), slashCommand.getId());
					}
					log.info("Registered all slash commands.");
					return updatePermissions(api, commandConfigs, nameToId)
							.thenRun(() -> log.info("Updated permissions for all slash commands."))
							.thenApplyAsync(unused -> handlersById);
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
					log.error("An error occurred when trying to get new instance of a slash command handler class.", e);
				}
			} else {
				log.error("Command {} does not have an associated slash command handler.", commandConfig.getName());
			}
		}
		return handlers;
	}

	private CompletableFuture<Void> deleteAllSlashCommands(DiscordApi api) {
		var serverDeleteFutures = api.getServers().stream()
				.map(server -> api.bulkOverwriteServerSlashCommands(server, List.of()))
				.toList();
		return api.bulkOverwriteGlobalSlashCommands(List.of())
				.thenComposeAsync(unused -> CompletableFuture.allOf(serverDeleteFutures.toArray(new CompletableFuture[0])));
	}

	private CompletableFuture<Void> updatePermissions(DiscordApi api, CommandConfig[] commandConfigs, Map<String, Long> nameToId) {
		List<CompletableFuture<?>> permissionFutures = new ArrayList<>();
		for (var server : api.getServers()) {
			List<ServerSlashCommandPermissionsBuilder> permissionsBuilders = new ArrayList<>();
			for (var config : commandConfigs) {
				if (config.getPrivileges() != null && config.getPrivileges().length > 0) {
					List<SlashCommandPermissions> permissions = Arrays.stream(config.getPrivileges())
							.map(p -> p.toData(server)).toList();
					long commandId = nameToId.get(config.getName());
					var builder = new ServerSlashCommandPermissionsBuilder(commandId, permissions);
					permissionsBuilders.add(builder);
				}
			}
			permissionFutures.add(api.batchUpdateSlashCommandPermissions(server, permissionsBuilders));
		}
		return CompletableFuture.allOf(permissionFutures.toArray(new CompletableFuture[0]));
	}
}
