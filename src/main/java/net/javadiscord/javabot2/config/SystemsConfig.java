package net.javadiscord.javabot2.config;

import lombok.Data;

/**
 * Contains configuration settings for various systems which the bot uses, such
 * as databases or dependencies that have runtime properties.
 */
@Data
public class SystemsConfig {
	private static final int DEFAULT_ASYNC_POOL_SIZE = 4;

	/**
	 * The token used to create the Discord bot instance.
	 */
	private String discordBotToken = "";

	/**
	 * The URL used to log in to the MongoDB instance which this bot uses.
	 */
	private String mongoDatabaseUrl = "mongodb://root:example@localhost:27171";

	/**
	 * The number of threads to allocate to the bot's general purpose async
	 * thread pool.
	 */
	private int asyncPoolSize = DEFAULT_ASYNC_POOL_SIZE;

	/**
	 * Configuration for the Hikari connection pool that's used for the bot's
	 * SQL data source.
	 */
	private HikariConfig hikariConfig = new HikariConfig();

	/**
	 * Configuration settings for the Hikari connection pool.
	 */
	@Data
	public static class HikariConfig {
		private static final int DEFAULT_POOL_SIZE = 5;
		private String jdbcUrl = "jdbc:h2:tcp://localhost:9123/./javabot";
		private int maximumPoolSize = DEFAULT_POOL_SIZE;
	}
}
