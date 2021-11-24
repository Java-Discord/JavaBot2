package net.javadiscord.javabot2.command.data;

import lombok.Data;
import org.javacord.api.interaction.SlashCommandOptionBuilder;
import org.javacord.api.interaction.SlashCommandOptionType;

import java.util.Arrays;

/**
 * Simple DTO representing an option that can be given to a Discord slash
 * command or subcommand.
 */
@Data
public class OptionConfig {
	private String name;
	private String description;
	private String type;
	private boolean required;
	private OptionChoiceConfig[] choices;

	/**
	 * Converts this config data into data that's ready for the Discord API.
	 * @return The prepared data.
	 */
	public SlashCommandOptionBuilder toData() {
		var builder = new SlashCommandOptionBuilder()
				.setType(SlashCommandOptionType.valueOf(this.type.trim().toUpperCase()))
				.setName(this.name)
				.setDescription(this.description)
				.setRequired(this.required);
		if (this.choices != null && this.choices.length > 0) {
			builder.setChoices(Arrays.stream(this.choices).map(OptionChoiceConfig::toData).toList());
		}
		return builder;
	}
}
