package net.javadiscord.javabot2;

import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.interaction.SlashCommandBuilder;

import java.util.List;

public class Bot {
	public static void main(String[] args) {
		DiscordApi api = new DiscordApiBuilder().setToken("").login().join();
		api.bulkOverwriteGlobalSlashCommands(List.of(
				new SlashCommandBuilder().setName("test")
		));
	}
}
