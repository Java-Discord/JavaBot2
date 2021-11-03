package net.javadiscord.javabot2.command.data;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import net.javadiscord.javabot2.Bot;
import net.javadiscord.javabot2.config.UnknownPropertyException;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.interaction.SlashCommandPermissionType;
import org.javacord.api.interaction.SlashCommandPermissions;
import org.javacord.api.interaction.SlashCommandPermissionsBuilder;

/**
 * Simple DTO that represents permissions information that can be attached to
 * a slash command.
 */
@Data
@Slf4j
public class CommandPrivilegeConfig {
	private String type;
	private boolean enabled = true;
	private String id;

	/**
	 * Converts this config data into data that's ready for the Discord API.
	 * @param server The server that the permissions will apply to.
	 * @return The prepared data.
	 */
	public SlashCommandPermissions toData(Server server) {
		if (this.type.equalsIgnoreCase(SlashCommandPermissionType.USER.name())) {
			User user = server.getMemberById(this.id).orElseThrow();
			return new SlashCommandPermissionsBuilder()
					.setType(SlashCommandPermissionType.USER)
					.setId(user.getId())
					.setPermission(true)
					.build();
		} else if (this.type.equalsIgnoreCase(SlashCommandPermissionType.ROLE.name())) {
			Long roleId = null;
			try {
				roleId = (Long) Bot.config.get(server).resolve(this.id);
			} catch (UnknownPropertyException e) {
				log.error("Unknown property while resolving role id.", e);
			}
			if (roleId == null) throw new IllegalArgumentException("Missing role id.");
			Role role = server.getRoleById(roleId).orElseThrow();
			return new SlashCommandPermissionsBuilder()
					.setType(SlashCommandPermissionType.ROLE)
					.setId(role.getId())
					.setPermission(true)
					.build();
		}
		throw new IllegalArgumentException("Invalid permission type.");
	}
}
