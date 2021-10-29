package net.javadiscord.javabot2.command;

import org.javacord.api.interaction.SlashCommandInteraction;
import org.javacord.api.interaction.callback.InteractionImmediateResponseBuilder;

public interface SlashCommandHandler {
	InteractionImmediateResponseBuilder handle(SlashCommandInteraction interaction) throws Exception;
}
