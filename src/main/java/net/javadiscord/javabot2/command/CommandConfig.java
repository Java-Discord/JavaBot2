package net.javadiscord.javabot2.command;

import lombok.Data;
import org.javacord.api.interaction.SlashCommandBuilder;

import java.util.Arrays;

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
	public String toString() {
		return "CommandConfig{" +
			"name='" + name + '\'' +
			", description='" + description + '\'' +
			", options=" + Arrays.toString(options) +
			", subCommands=" + Arrays.toString(subCommands) +
			", subCommandGroups=" + Arrays.toString(subCommandGroups) +
			", handler=" + handler +
			'}';
	}
}
