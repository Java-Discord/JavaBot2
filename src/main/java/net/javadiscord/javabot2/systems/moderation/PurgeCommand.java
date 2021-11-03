package net.javadiscord.javabot2.systems.moderation;

import net.javadiscord.javabot2.command.SlashCommandHandler;
import org.javacord.api.interaction.SlashCommandInteraction;
import org.javacord.api.interaction.callback.InteractionImmediateResponseBuilder;

public class PurgeCommand implements SlashCommandHandler {
	@Override
	public InteractionImmediateResponseBuilder handle(SlashCommandInteraction interaction) {
		return interaction.createImmediateResponder()
				.append("Not yet implemented");
	}
}
