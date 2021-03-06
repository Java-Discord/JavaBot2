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
	/**
	 * The set of all slash command handlers, mapped by their names.
	 */
	private final Map<String, SlashCommandHandler> commandHandlers;

	/**
	 * Constructs a new slash command listener using the given Discord api, and
	 * loads commands from configuration YAML files according to the list of
	 * resources.
	 * @param api The Discord api to use.
	 * @param sendUpdate Set to true if we should update the slash commands in
	 *                   the API, or false if we should just assume it's done.
	 * @param resources The list of classpath resources to load commands from.
	 */
	public SlashCommandListener(DiscordApi api, boolean sendUpdate, String... resources) {
		if (sendUpdate) {
			this.commandHandlers = new HashMap<>();
			registerSlashCommands(api, resources)
					.thenAcceptAsync(commandHandlers::putAll)
					.thenRun(() -> log.info("Registered all slash commands."));
		} else {
			this.commandHandlers = initializeHandlers(CommandDataLoader.load(resources));
			log.info("Registered all slash commands.");
		}
	}

	@Override
	public void onSlashCommandCreate(SlashCommandCreateEvent event) {
		var handler = commandHandlers.get(event.getSlashCommandInteraction().getCommandName());
		if (handler != null) {
			try {
				handler.handle(event.getSlashCommandInteraction()).respond();
			} catch (ResponseException e) {
				e.getResponseBuilder().respond(event.getSlashCommandInteraction());
			}
		} else {
			Responses.warningBuilder(event)
					.title("No Handler")
					.message("There is no associated handler for this command. Please contact an administrator if this error persists.")
					.respond();
		}
	}

	private CompletableFuture<Map<String, SlashCommandHandler>> registerSlashCommands(DiscordApi api, String... resources) {
		var commandConfigs = CommandDataLoader.load(resources);
		var handlers = initializeHandlers(commandConfigs);
		List<SlashCommandBuilder> commandBuilders = Arrays.stream(commandConfigs)
				.map(CommandConfig::toData).toList();
		return deleteAllSlashCommands(api)
				.thenComposeAsync(unused -> api.bulkOverwriteGlobalSlashCommands(commandBuilders))
				.thenComposeAsync(slashCommands -> {
					Map<String, Long> nameToId = new HashMap<>();
					for (var slashCommand : slashCommands) {
						nameToId.put(slashCommand.getName(), slashCommand.getId());
					}
					return updatePermissions(api, commandConfigs, nameToId)
							.thenRun(() -> log.info("Updated permissions for all slash commands."))
							.thenApplyAsync(unused -> handlers);
				});
	}

	private Map<String, SlashCommandHandler> initializeHandlers(CommandConfig[] commandConfigs) {
		Map<String, SlashCommandHandler> handlers = new HashMap<>();
		for (var commandConfig : commandConfigs) {
			if (commandConfig.getHandler() != null && !commandConfig.getHandler().isBlank()) {
				try {
					Class<?> handlerClass = Class.forName(commandConfig.getHandler());
					handlers.put(commandConfig.getName(), (SlashCommandHandler) handlerClass.getConstructor().newInstance());
				} catch (ReflectiveOperationException e) {
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
