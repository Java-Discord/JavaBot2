package net.javadiscord.javabot2.config;

import lombok.Data;

/**
 * Contains configuration settings for various systems which the bot uses, such
 * as databases or dependencies that have runtime properties.
 */
@Data
public class SystemsConfig {
	/**
	 * The token used to create the Discord bot instance.
	 */
	private String discordBotToken = "";

	/**
	 * The URL used to log in to the MongoDB instance which this bot uses.
	 */
	private String mongoDatabaseUrl = "mongodb://root:example@localhost:27171/javabot";

	/**
	 * The number of threads to allocate to the bot's general purpose async
	 * thread pool.
	 */
	private int asyncPoolSize = 4;

	/**
	 * Configuration for the Hikari connection pool that's used for the bot's
	 * SQL data source.
	 */
	private HikariConfig hikariConfig = new HikariConfig();

	@Data
	public static class HikariConfig {
		private String jdbcUrl = "jdbc:postgresql://localhost:27172/javabot";
		private String username = "javabot_dev";
		private String password = "javabot_dev_pass";
		private int maximumPoolSize = 5;
		private String connectionInitSql = "SET TIME ZONE 'UTC'";
	}
}
