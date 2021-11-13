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
import net.javadiscord.javabot2.systems.moderation.ModerationService;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.intent.Intent;

import java.nio.file.Path;
import java.sql.SQLException;
import java.time.ZoneOffset;
import java.util.TimeZone;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

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
		initScheduledTasks(api);
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

		try {
			hikariDataSource = DbHelper.initDataSource(config);
		} catch (SQLException e) {
			log.error("Could not initialize Hikari data source.");
			throw new IllegalStateException(e);
		}
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

	private static void initScheduledTasks(DiscordApi api) {
		// Regularly check for and unmute users whose mutes have expired.
		asyncPool.scheduleAtFixedRate(() -> {
			for (var server : api.getServers()) {
				new ModerationService(api, config.get(server).getModeration()).unmuteExpired();
			}
		}, 1L, 1L, TimeUnit.MINUTES);
	}
}
