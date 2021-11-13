package net.javadiscord.javabot2.systems.moderation;

import net.javadiscord.javabot2.command.ResponseException;
import net.javadiscord.javabot2.command.Responses;
import net.javadiscord.javabot2.command.SlashCommandHandler;
import org.javacord.api.entity.user.User;
import org.javacord.api.interaction.SlashCommandInteraction;
import org.javacord.api.interaction.callback.InteractionImmediateResponseBuilder;

/**
 * Command which is used to ban naughty users.
 */
public class BanCommand implements SlashCommandHandler {
	@Override
	public InteractionImmediateResponseBuilder handle(SlashCommandInteraction interaction) throws ResponseException {
		User user = interaction.getOptionUserValueByName("user")
				.orElseThrow(ResponseException.warning("User is required."));
		String reason = interaction.getOptionStringValueByName("reason")
				.orElseThrow(ResponseException.warning("Reason is required."));
		var channel = interaction.getChannel()
				.orElseThrow(ResponseException.warning("This command can only be performed in a channel."))
				.asServerTextChannel()
				.orElseThrow(ResponseException.warning("This command can only be performed in a server text channel."));
		var quiet = interaction.getOptionBooleanValueByName("quiet").orElse(false);
		var moderationService = new ModerationService(interaction);
		moderationService.ban(user, reason, interaction.getUser(), channel, quiet);
		return Responses.successBuilder(interaction)
				.title("User Banned")
				.messageFormat("User %s has been banned.", user.getMentionTag())
				.build();
	}
}
