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
	 * The amount of seconds to be looked into the past to determine if a user is spamming.
	 */
	private int pastMessageCountBeforeDurationInSeconds;

	/**
	 * The amount of messages to be sent within {@link #pastMessageCountBeforeDurationInSeconds}.
	 */
	private int messageSpamAmount;

	/**
	 * The amount of messages to be cached for each user.
	 */
	private int cachedMessagesPerUser;

	/**
	 * The frequency of cleaning up the cached messages. Amount in minutes.
	 */
	private int cachedMessageCleanupFrequency;

	/**
	 * The amount of minutes for removing this cached messages.
	 */
	private int amountOfMinutesForRemoval;

	public Role getStaffRole() {
		return this.getGuild().getRoleById(staffRoleId).orElseThrow();
	}
}
