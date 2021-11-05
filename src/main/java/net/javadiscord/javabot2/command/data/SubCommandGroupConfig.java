package net.javadiscord.javabot2.command.data;

import lombok.Data;
import org.javacord.api.interaction.SlashCommandOptionBuilder;
import org.javacord.api.interaction.SlashCommandOptionType;

/**
 * Simple DTO for a group of Discord subcommands.
 */
@Data
public class SubCommandGroupConfig {
	private String name;
	private String description;
	private SubCommandConfig[] subCommands;

	/**
	 * Converts this config data into data that's ready for the Discord API.
	 * @return The prepared data.
	 */
	public SlashCommandOptionBuilder toData() {
		var builder = new SlashCommandOptionBuilder()
				.setType(SlashCommandOptionType.SUB_COMMAND_GROUP)
				.setName(this.name)
				.setDescription(this.description);
		if (this.subCommands != null) {
			for (var subCommand : this.subCommands) {
				builder.addOption(subCommand.toData().build());
			}
		}
		return builder;
	}
}
