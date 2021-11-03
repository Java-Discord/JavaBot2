package net.javadiscord.javabot2.config;

import lombok.Setter;
import org.javacord.api.entity.server.Server;

/**
 * Parent class for any guild-specific collection of configuration settings,
 * which exposes a link to the parent guild config, and thus, the guild, which
 * may be needed to obtain text channels.
 */
public abstract class GuildConfigItem {
	/**
	 * A reference to the parent config for this item.
	 */
	@Setter
	protected transient GuildConfig guildConfig;

	/**
	 * A shortcut to get the guild in the context of a specific config item.
	 * @return The guild that this item exists under.
	 */
	public Server getGuild() {
		return this.guildConfig.getGuild();
	}
}
