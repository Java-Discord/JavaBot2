package net.javadiscord.javabot2.command.data;

import lombok.Data;
import org.javacord.api.interaction.SlashCommandOptionBuilder;
import org.javacord.api.interaction.SlashCommandOptionChoiceBuilder;
import org.javacord.api.interaction.SlashCommandOptionType;

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

	public SlashCommandOptionBuilder toData() {
		return new SlashCommandOptionBuilder()
				.setType(SlashCommandOptionType.valueOf(this.type.toUpperCase()))
				.setName(this.name)
				.setDescription(this.description)
				.setRequired(this.required);
	}

	@Override
	public String toString() {
		return "OptionConfig{" +
			"name='" + name + '\'' +
			", description='" + description + '\'' +
			", type='" + type + '\'' +
			", required=" + required +
			'}';
	}
}
