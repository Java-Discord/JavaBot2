package net.javadiscord.javabot2;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import net.javadiscord.javabot2.command.SlashCommandListener;
import net.javadiscord.javabot2.config.BotConfig;
import net.javadiscord.javabot2.db.DbHelper;
import net.javadiscord.javabot2.tasks.ScheduledTasks;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.intent.Intent;
import org.quartz.SchedulerException;

import java.nio.file.Path;
import java.time.ZoneOffset;
import java.util.TimeZone;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * The main program entry point.
 */
@Slf4j
public class Bot {
	/**
	 * A connection pool that can be used to obtain new JDBC connections.
	 */
	public static HikariDataSource hikariDataSource;

	/**
	 * A thread-safe MongoDB client that can be used to interact with MongoDB.
	 * @deprecated Use the relational data source for all future persistence
	 * needs; it promotes more organized code that's less prone to failures.
	 */
	@Deprecated
	public static MongoClient mongoClient;

	/**
	 * The single Mongo database where all bot data is stored.
	 * @deprecated Use the relational data source for all future persistence
	 * needs; it promotes more organized code that's less prone to failures.
	 */
	@Deprecated
	public static MongoDatabase mongoDb;

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
		TimeZone.setDefault(TimeZone.getTimeZone(ZoneOffset.UTC));
		initDataSources();
		asyncPool = Executors.newScheduledThreadPool(config.getSystems().getAsyncPoolSize());
		DiscordApi api = new DiscordApiBuilder()
				.setToken(config.getSystems().getDiscordBotToken())
				.setAllIntentsExcept(Intent.GUILD_MESSAGE_TYPING, Intent.GUILD_PRESENCES, Intent.GUILD_VOICE_STATES)
				.login().join();
		config.loadGuilds(api.getServers()); // Once we've logged in, load all guild config files.
		config.flush(); // Flush to save any new config files that are generated for new guilds.
		SlashCommandListener commandListener = new SlashCommandListener(
				api,
				args.length > 0 && args[0].equalsIgnoreCase("--register-commands"),
				"commands/moderation.yaml"
		);
		api.addSlashCommandCreateListener(commandListener);
		try {
			ScheduledTasks.init(api);
			log.info("Initialized scheduled tasks.");
		} catch (SchedulerException e) {
			log.error("Could not initialize all scheduled tasks.", e);
			api.disconnect().join();
		}
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
		hikariDataSource = DbHelper.initDataSource(config);
		mongoDb = initMongoDatabase();
	}

	private static MongoDatabase initMongoDatabase() {
		mongoClient = new MongoClient(new MongoClientURI(config.getSystems().getMongoDatabaseUrl()));
		var db = mongoClient.getDatabase("javabot");
		var warnCollection = db.getCollection("warn");
		warnCollection.createIndex(Indexes.ascending("userId"), new IndexOptions().unique(false));
		warnCollection.createIndex(Indexes.descending("createdAt"), new IndexOptions().unique(false));
		return db;
	}
}
