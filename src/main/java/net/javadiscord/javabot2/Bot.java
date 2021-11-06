package net.javadiscord.javabot2;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import net.javadiscord.javabot2.command.SlashCommandListener;
import net.javadiscord.javabot2.config.BotConfig;
import net.javadiscord.javabot2.systems.moderation.SpamListener;
import net.javadiscord.javabot2.systems.moderation.MessageCache;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.intent.Intent;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageAuthor;

import java.nio.file.Path;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;
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
	public static MongoClient mongoClient;

	/**
	 * The single Mongo database where all bot data is stored.
	 */
	public static MongoDatabase mongoDb;

	/**
	 * The bot's configuration.
	 */
	public static BotConfig config;

	/**
	 * A general purpose thread pool for asynchronous tasks.
	 */
	public static ScheduledExecutorService asyncPool;

	/**
	 * The message cache.
	 */
	public static ConcurrentHashMap<MessageAuthor, LinkedList<Message>> messageCache;

	// Hide constructor.
	private Bot() {}

	/**
	 * Starts the bot.
	 * @param args Command-line arguments.
	 */
	public static void main(String[] args) {
		initDataSources();
		asyncPool = Executors.newScheduledThreadPool(config.getSystems().getAsyncPoolSize());
		DiscordApi api = new DiscordApiBuilder()
				.setToken(config.getSystems().getDiscordBotToken())
				.setAllIntentsExcept(Intent.GUILD_MESSAGE_TYPING, Intent.GUILD_PRESENCES, Intent.GUILD_VOICE_STATES)
				.login().join();

		initListeners(api);

		config.loadGuilds(api.getServers()); // Once we've logged in, load all guild config files.
		config.flush(); // Flush to save any new config files that are generated for new guilds.

		SlashCommandListener commandListener = new SlashCommandListener(
				api,
				args.length > 0 && args[0].equalsIgnoreCase("--register-commands"),
				"commands/moderation.yaml"
		);
		api.addSlashCommandCreateListener(commandListener);
	}

	/**
	 * Initializes and adds all listeners to the API.
	 * @param api the API
	 */
	private static void initListeners(DiscordApi api) {
		MessageCache cache = new MessageCache();
		messageCache = cache.getCache();

		api.addMessageCreateListener(new SpamListener());
		api.addMessageCreateListener(cache);
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
		mongoDb = initMongoDatabase();
	}

	private static MongoDatabase initMongoDatabase() {
		mongoClient = new MongoClient(new MongoClientURI(config.getSystems().getMongoDatabaseUrl()));
		var db = mongoClient.getDatabase("javabot");
		var warnCollection = db.getCollection("warn");
		warnCollection.createIndex(Indexes.ascending("userId"), new IndexOptions().unique(false));
		return db;
	}
}
