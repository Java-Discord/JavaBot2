package net.javadiscord.javabot2.command.data;

import lombok.Data;
import org.javacord.api.interaction.SlashCommandBuilder;

import java.util.Objects;

/**
 * Simple DTO representing a top-level Discord slash command.
 */
@Data
public class CommandConfig {
	private String name;
	private String description;
	private boolean enabledByDefault = true;
	private CommandPrivilegeConfig[] privileges;
	private OptionConfig[] options;
	private SubCommandConfig[] subCommands;
	private SubCommandGroupConfig[] subCommandGroups;
	private String handler;

	/**
	 * Converts this config data into data that's ready for the Discord API.
	 * @return The prepared data.
	 */
	public SlashCommandBuilder toData() {
		var builder = new SlashCommandBuilder()
				.setName(this.name)
				.setDescription(this.description)
				.setDefaultPermission(this.enabledByDefault);
		if (this.options != null) {
			for (var option : this.options) {
				builder.addOption(option.toData().build());
			}
		}
		if (this.subCommands != null) {
			for (var subCommand : this.subCommands) {
				builder.addOption(subCommand.toData().build());
			}
		}
		if (this.subCommandGroups != null) {
			for (var group : this.subCommandGroups) {
				builder.addOption(group.toData().build());
			}
		}
		return builder;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof CommandConfig that)) return false;
		return getName().equals(that.getName());
	}

	@Override
	public int hashCode() {
		return Objects.hash(getName());
	}
}
