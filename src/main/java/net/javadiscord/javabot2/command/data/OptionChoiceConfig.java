package net.javadiscord.javabot2.command.data;

import lombok.Data;
import org.javacord.api.interaction.SlashCommandOptionChoice;
import org.javacord.api.interaction.SlashCommandOptionChoiceBuilder;

/**
 * DTO for a choice that a slash command option can have.
 */
@Data
public class OptionChoiceConfig {
	private String name;
	private String value;

	/**
	 * Converts this choice data into a Javacord object for use with the API.
	 * @return The Javacord option choice object.
	 */
	public SlashCommandOptionChoice toData() {
		return new SlashCommandOptionChoiceBuilder()
				.setName(name)
				.setValue(value)
				.build();
	}
}
