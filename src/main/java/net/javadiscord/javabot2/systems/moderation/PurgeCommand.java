package net.javadiscord.javabot2.systems.moderation;

import lombok.extern.slf4j.Slf4j;
import net.javadiscord.javabot2.Bot;
import net.javadiscord.javabot2.command.ResponseException;
import net.javadiscord.javabot2.command.Responses;
import net.javadiscord.javabot2.command.SlashCommandHandler;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.user.User;
import org.javacord.api.interaction.SlashCommandInteraction;
import org.javacord.api.interaction.callback.InteractionImmediateResponseBuilder;

/**
 * This command allows staff to purge many messages from a text channel.
 */
@Slf4j
public class PurgeCommand implements SlashCommandHandler {
	@Override
	public InteractionImmediateResponseBuilder handle(SlashCommandInteraction interaction) throws ResponseException {
		var until = interaction.getOptionStringValueByName("until")
				.orElseThrow(ResponseException.warning("Missing required parameter."));
		var userOption = interaction.getOptionUserValueByName("user");
		var channel = interaction.getChannel()
				.orElseThrow(ResponseException.warning("This command can only be used in text channels."));
		channel.getMessageById(until).thenAcceptAsync(message -> Bot.asyncPool.submit(() -> purge(message, userOption.orElse(null))));
		return Responses.info(interaction, "Purge Started", "Messages will be deleted!");
	}

	/**
	 * Purges messages from a channel.
	 * @param until The message after which all should be removed.
	 * @param user The user to remove messages for. This may be null.
	 */
	private void purge(Message until, User user) {
		log.info("Purging all messages in {} until {}.", until.getServerTextChannel().orElseThrow().getName(), until.getId());
		until.getMessagesAfterAsStream()
				.filter(message -> user == null || message.getAuthor().getId() == user.getId())
				.forEach(message -> message.delete().join());
		if (user == null || until.getAuthor().getId() == user.getId()) {
			until.delete().join();
		}
		log.info("Purge completed.");
	}
}
