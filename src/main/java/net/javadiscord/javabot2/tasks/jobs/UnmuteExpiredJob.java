package net.javadiscord.javabot2.tasks.jobs;

import net.javadiscord.javabot2.Bot;
import net.javadiscord.javabot2.systems.moderation.ModerationService;
import org.javacord.api.DiscordApi;
import org.quartz.JobExecutionContext;

/**
 * Job which unmutes users whose mutes have expired.
 */
public class UnmuteExpiredJob extends DiscordApiJob {
	@Override
	protected void execute(JobExecutionContext context, DiscordApi api) {
		for (var server : api.getServers()) {
			new ModerationService(api, Bot.config.get(server).getModeration()).unmuteExpired();
		}
	}
}
