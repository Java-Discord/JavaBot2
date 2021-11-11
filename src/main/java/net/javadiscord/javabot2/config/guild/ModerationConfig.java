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

	/**
	 * The number of days for which a user's warning may contribute to them
	 * being removed from the server. Warnings older than this are still kept,
	 * but ignored.
	 */
	private int warnTimeoutDays;

	/**
	 * The maximum total severity that a user can accrue from warnings before
	 * being removed from the server.
	 */
	private int maxWarnSeverity;

	public Role getStaffRole() {
		return this.getGuild().getRoleById(staffRoleId).orElseThrow();
	}
}
