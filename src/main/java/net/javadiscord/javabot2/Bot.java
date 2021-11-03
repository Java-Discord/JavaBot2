package net.javadiscord.javabot2;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import net.javadiscord.javabot2.command.SlashCommandListener;
import net.javadiscord.javabot2.config.BotConfig;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;

import java.nio.file.Path;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * The main program entry point.
 */
public class Bot {
	/**
	 * A connection pool that can be used to obtain new JDBC connections.
	 */
	public static HikariDataSource hikariDataSource;

	/**
	 * A thread-safe MongoDB client that can be used to interact with MongoDB.
	 */
	public static MongoClient mongo;

	/**
	 * The bot's configuration.
	 */
	public static BotConfig config;

	/**
	 * A general purpose thread pool for asynchronous tasks.
	 */
	public static ScheduledExecutorService asyncPool;

	// Hide constructor.
	private Bot() {}

	/**
	 * Starts the bot.
	 * @param args Command-line arguments.
	 */
	public static void main(String[] args) {
		initDataSources();
		asyncPool = Executors.newScheduledThreadPool(config.getSystems().getAsyncPoolSize());
		DiscordApi api = new DiscordApiBuilder().setToken(config.getSystems().getDiscordBotToken()).login().join();
		config.loadGuilds(api.getServers()); // Once we've logged in, load all guild config files.
		config.flush(); // Flush to save any new config files that are generated for new guilds.
		SlashCommandListener commandListener = new SlashCommandListener(
				api,
				"commands/moderation.yaml"
		);
		api.addSlashCommandCreateListener(commandListener);
	}

	/**
	 * Initializes all the basic data sources that are needed by the bot's other
	 * capabilities. This should be called <strong>before</strong> logging in
	 * with the Discord API.
	 */
	private static void initDataSources() {
		config = new BotConfig(Path.of("javabot_config"));
		if (config.getSystems().getDiscordBotToken() == null || config.getSystems().getDiscordBotToken().isBlank()) {
			throw new IllegalStateException("Missing required Discord bot token! Please edit config/systems.json to add it, then run again.");
		}
		var hikariConfig = new HikariConfig();
		var hikariConfigSource = config.getSystems().getHikariConfig();
		hikariConfig.setJdbcUrl(hikariConfigSource.getJdbcUrl());
		hikariConfig.setUsername(hikariConfigSource.getUsername());
		hikariConfig.setPassword(hikariConfigSource.getPassword());
		hikariConfig.setMaximumPoolSize(hikariConfigSource.getMaximumPoolSize());
		hikariConfig.setConnectionInitSql(hikariConfigSource.getConnectionInitSql());
		hikariDataSource = new HikariDataSource(hikariConfig);
		mongo = new MongoClient(new MongoClientURI(config.getSystems().getMongoDatabaseUrl()));
	}
}
