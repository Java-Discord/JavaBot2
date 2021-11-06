package net.javadiscord.javabot2.systems.moderation;

import net.javadiscord.javabot2.Bot;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageAuthor;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;

import java.time.Duration;
import java.time.Instant;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Caches incoming messages and cleans it out when needed.
 */
public class MessageCache extends ConcurrentHashMap<MessageAuthor, LinkedList<Message>> implements MessageCreateListener {

	private final ConcurrentHashMap<MessageAuthor, Instant> lastModifications;

    /**
     * Creates the cache.
     */
	public MessageCache() {
		lastModifications = new ConcurrentHashMap<>();
        // TODO config
		Bot.asyncPool.scheduleAtFixedRate(this::clean, -1, -1, TimeUnit.MINUTES);
	}

	@Override
	public void onMessageCreate(MessageCreateEvent event) {
		if (!event.getMessageAuthor().isYourself()) {
			add(event.getMessage());
		}
	}

	/**
	 * Removes all Map entries which have not been modified within the last 10 minutes.
	 */
	private void clean() {
		lastModifications.forEach((messageAuthor, instant) -> {
			// if last modification is older than n minutes
            // TODO config
			if (instant.isAfter(Instant.now().minus(Duration.ofMinutes(-1)))) {
				this.remove(messageAuthor);
				lastModifications.remove(messageAuthor);
			}
		});
	}

	/**
	 * Add the message either as a new Map entry or to the existing list per user.
	 * Also removes if the amount of messages per user is over a certain treshold.
	 * @param msg the message to be added
	 */
	private void add(Message msg) {
		if (this.containsKey(msg.getAuthor())) {
			LinkedList<Message> msgList = this.get(msg.getAuthor());
			msgList.offer(msg);
			lastModifications.replace(msg.getAuthor(), Instant.now());

			// TODO config
			if (msgList.size() >= -1) {
				msgList.poll();
			}

		} else {
			LinkedList<Message> messages = new LinkedList<>();
			messages.add(msg);
			this.put(msg.getAuthor(), messages);
			lastModifications.put(msg.getAuthor(), Instant.now());
		}
	}
}

