package net.javadiscord.javabot2.systems.moderation;

import net.javadiscord.javabot2.command.SlashCommandHandler;
import org.javacord.api.interaction.SlashCommandInteraction;
import org.javacord.api.interaction.callback.InteractionImmediateResponseBuilder;

public class PruneCommand implements SlashCommandHandler {
	@Override
	public InteractionImmediateResponseBuilder handle(SlashCommandInteraction interaction) throws Exception {
		return interaction.createImmediateResponder()
				.append("Not yet implemented");
	}
}
