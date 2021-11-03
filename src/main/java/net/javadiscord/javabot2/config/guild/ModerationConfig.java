package net.javadiscord.javabot2.config.guild;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.javadiscord.javabot2.config.GuildConfigItem;
import org.javacord.api.entity.permission.Role;

/**
 * Configuration for the server's moderation.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ModerationConfig extends GuildConfigItem {
	/**
	 * The id of the server's staff role.
	 */
	private long staffRoleId;

	public Role getStaffRole() {
		return this.getGuild().getRoleById(staffRoleId).orElseThrow();
	}
}
