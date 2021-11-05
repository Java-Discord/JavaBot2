package net.javadiscord.javabot2.command.data;

import lombok.Data;
import org.javacord.api.interaction.SlashCommandOptionChoice;
import org.javacord.api.interaction.SlashCommandOptionChoiceBuilder;

@Data
public class OptionChoiceConfig {
	private String name;
	private String value;

	public SlashCommandOptionChoice toData() {
		return new SlashCommandOptionChoiceBuilder()
				.setName(name)
				.setValue(value)
				.build();
	}
}
