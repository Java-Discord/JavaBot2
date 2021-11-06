package net.javadiscord.javabot2.systems.moderation;

import net.javadiscord.javabot2.Bot;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;

import java.time.Duration;
import java.time.Instant;

/**
 * Listens for spam using the {@link MessageCache}.
 */
public class SpamListener implements MessageCreateListener {

	@Override
	public void onMessageCreate(MessageCreateEvent event) {
		if (!event.getMessageAuthor().isYourself() && Bot.messageCache.containsKey(event.getMessageAuthor())) {
			int amountOfMessages = (int) Bot.messageCache.get(event.getMessageAuthor())
					.stream()
					//TODO get time from config
					.filter(m -> m.getCreationTimestamp().isAfter(Instant.now().minus(Duration.ofSeconds(-1))))
					.count();

			System.out.println(amountOfMessages);

			//TODO get amount from config
			if (amountOfMessages >= -1) {
				//TODO spam detected
				// placeholder for checkstyle not to complain.
				// should be replaced with a warn + purge which is not implemented yet
				event.deleteMessage("spam");
			}
		}
	}

}
