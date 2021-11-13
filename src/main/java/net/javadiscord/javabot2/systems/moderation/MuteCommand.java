package net.javadiscord.javabot2.systems.moderation;

import net.javadiscord.javabot2.command.ResponseException;
import net.javadiscord.javabot2.command.Responses;
import net.javadiscord.javabot2.command.SlashCommandHandler;
import org.javacord.api.interaction.SlashCommandInteraction;
import org.javacord.api.interaction.callback.InteractionImmediateResponseBuilder;

import java.time.Duration;
import java.time.format.DateTimeParseException;

/**
 * Command that mutes a user.
 */
public class MuteCommand implements SlashCommandHandler {
	@Override
	public InteractionImmediateResponseBuilder handle(SlashCommandInteraction interaction) throws ResponseException {
		var user = interaction.getOptionUserValueByName("user")
				.orElseThrow(ResponseException.warning("Missing required user."));
		if (user.isBot()) return Responses.warning(interaction, "Cannot mute bots.");
		var reason = interaction.getOptionStringValueByName("reason")
				.orElseThrow(ResponseException.warning("Missing reason."));
		var duration = interaction.getOptionStringValueByName("duration")
				.orElse("PT30M");
		var quiet = interaction.getOptionBooleanValueByName("quiet").orElse(false);
		var channel = interaction.getChannel()
				.orElseThrow(ResponseException.warning("This command can only be used in a text channel."))
				.asServerTextChannel().orElseThrow(ResponseException.warning("This command can only be used in a server."));
		Duration d;
		try {
			d = Duration.parse(duration);
		} catch (DateTimeParseException e) {
			return Responses.warningBuilder(interaction)
					.title("Invalid Duration")
					.message("""
							You provided an invalid duration. Please use ISO-8601 format.
							Please see here for more info: https://en.wikipedia.org/wiki/ISO_8601#Durations
							For example, `P1D` means *1 day*, and `PT30M` means *30 minutes*.""")
					.build();
		}
		var moderationService = new ModerationService(interaction);
		moderationService.mute(user, reason, interaction.getUser(), d, channel, quiet);
		return Responses.successBuilder(interaction)
				.title("User Muted")
				.messageFormat("User %s has been muted.", user.getDiscriminatedName())
				.build();
	}
}
