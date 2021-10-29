package net.javadiscord.javabot2;

import net.javadiscord.javabot2.command.CommandDataLoader;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.interaction.SlashCommandBuilder;

import java.util.List;

public class Bot {
	public static void main(String[] args) {
		DiscordApi api = new DiscordApiBuilder().setToken("").login().join();
		var commands = CommandDataLoader.load("commands/moderation.yaml");
		api.bulkOverwriteGlobalSlashCommands(List.of(
				new SlashCommandBuilder().setName("test")
		));
	}
}
