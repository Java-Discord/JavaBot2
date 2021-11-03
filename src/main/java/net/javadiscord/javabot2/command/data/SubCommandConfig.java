package net.javadiscord.javabot2.command.data;

import lombok.Data;
import org.javacord.api.interaction.SlashCommandOptionBuilder;
import org.javacord.api.interaction.SlashCommandOptionType;

import java.util.Arrays;

/**
 * Simple DTO for a Discord subcommand.
 */
@Data
public class SubCommandConfig {
	private String name;
	private String description;
	private OptionConfig[] options;

	/**
	 * Converts this config data into data that's ready for the Discord API.
	 * @return The prepared data.
	 */
	public SlashCommandOptionBuilder toData() {
		var builder = new SlashCommandOptionBuilder()
				.setType(SlashCommandOptionType.SUB_COMMAND)
				.setName(this.name)
				.setDescription(this.description);
		if (this.options != null) {
			for (var option : this.options) {
				builder.addOption(option.toData().build());
			}
		}
		return builder;
	}

	@Override
	public String toString() {
		return "SubCommandConfig{" +
			"name='" + name + '\'' +
			", description='" + description + '\'' +
			", options=" + Arrays.toString(options) +
			'}';
	}
}
