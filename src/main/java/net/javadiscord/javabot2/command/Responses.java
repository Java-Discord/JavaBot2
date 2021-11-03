package net.javadiscord.javabot2.command;

import org.javacord.api.entity.message.MessageFlag;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.event.interaction.SlashCommandCreateEvent;
import org.javacord.api.interaction.InteractionBase;
import org.javacord.api.interaction.callback.InteractionImmediateResponseBuilder;
import org.javacord.api.interaction.callback.InteractionOriginalResponseUpdater;

import java.awt.*;
import java.util.concurrent.CompletableFuture;

/**
 * Provides methods for standardized replies to interaction events.
 */
public class Responses {
	public static InteractionImmediateResponseBuilder success(InteractionBase interaction, String title, String message) {
		return reply(interaction, title, message, Color.GREEN, true);
	}

	public static ResponseBuilder successBuilder(InteractionBase interaction) {
		return new ResponseBuilder(interaction, Color.GREEN).title("Success");
	}

	public static ResponseBuilder successBuilder(SlashCommandCreateEvent event) {
		return successBuilder(event.getSlashCommandInteraction());
	}

	public static InteractionImmediateResponseBuilder info(InteractionBase interaction, String title, String message) {
		return reply(interaction, title, message, Color.BLUE, true);
	}

	public static ResponseBuilder infoBuilder(InteractionBase interaction) {
		return new ResponseBuilder(interaction, Color.BLUE).title("Info");
	}

	public static ResponseBuilder infoBuilder(SlashCommandCreateEvent event) {
		return infoBuilder(event.getSlashCommandInteraction());
	}

	public static InteractionImmediateResponseBuilder warning(InteractionBase interaction, String title, String message) {
		return reply(interaction, title, message, Color.ORANGE, true);
	}

	public static InteractionImmediateResponseBuilder warning(InteractionBase interaction, String message) {
		return warning(interaction, "Warning", message);
	}

	public static ResponseBuilder warningBuilder(InteractionBase interaction) {
		return new ResponseBuilder(interaction, Color.ORANGE).title("Warning");
	}

	public static ResponseBuilder warningBuilder(SlashCommandCreateEvent event) {
		return warningBuilder(event.getSlashCommandInteraction());
	}

	public static ResponseBuilder deferredWarningBuilder() {
		return new ResponseBuilder(Color.ORANGE).title("Warning");
	}

	public static InteractionImmediateResponseBuilder error(InteractionBase interaction, String message) {
		return reply(interaction, "An Error Occurred", message, Color.RED, true);
	}

	public static ResponseBuilder errorBuilder(InteractionBase interaction) {
		return new ResponseBuilder(interaction, Color.RED).title("An Error Occurred");
	}

	public static ResponseBuilder errorBuilder(SlashCommandCreateEvent event) {
		return errorBuilder(event.getSlashCommandInteraction());
	}

	public static ResponseBuilder deferredErrorBuilder() {
		return new ResponseBuilder(Color.RED).title("An Error Occurred");
	}

	private static InteractionImmediateResponseBuilder reply(
			InteractionBase interaction,
			String title,
			String message,
			Color color,
			boolean ephemeral
	) {
		var responder = interaction.createImmediateResponder()
				.addEmbed(new EmbedBuilder()
					.setTitle(title)
					.setColor(color)
					.setTimestampToNow()
					.setDescription(message));
		if (ephemeral) {
			responder.setFlags(MessageFlag.EPHEMERAL);
		}
		return responder;
	}

	/**
	 * A builder that's used to construct a response using a fluent interface.
	 */
	public static class ResponseBuilder {
		private InteractionBase interaction;
		private String title = null;
		private String message = null;
		private final Color color;
		private boolean ephemeral = true;

		private ResponseBuilder(InteractionBase interaction, Color color) {
			this.interaction = interaction;
			this.color = color;
		}

		private ResponseBuilder(Color color) {
			this(null, color);
		}

		public ResponseBuilder title(String title) {
			this.title = title;
			return this;
		}

		public ResponseBuilder message(String message) {
			this.message = message;
			return this;
		}

		public ResponseBuilder makePublic() {
			this.ephemeral = false;
			return this;
		}

		public InteractionImmediateResponseBuilder build() {
			return reply(interaction, title, message, color, ephemeral);
		}

		public InteractionImmediateResponseBuilder build(InteractionBase interaction) {
			this.interaction = interaction;
			return build();
		}

		public CompletableFuture<InteractionOriginalResponseUpdater> respond(InteractionBase interaction) {
			this.interaction = interaction;
			return build().respond();
		}

		public CompletableFuture<InteractionOriginalResponseUpdater> respond() {
			return this.build().respond();
		}
	}
}
