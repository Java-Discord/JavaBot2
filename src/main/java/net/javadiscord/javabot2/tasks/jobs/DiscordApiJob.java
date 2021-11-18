package net.javadiscord.javabot2.tasks.jobs;

import org.javacord.api.DiscordApi;
import org.quartz.*;

import java.util.Map;

/**
 * A type of job which requires a reference to a {@link DiscordApi} to be
 * available at execution time. Extend this class if your job needs the api.
 */
public abstract class DiscordApiJob implements Job {
	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		DiscordApi api = (DiscordApi) context.getJobDetail().getJobDataMap().get("discord-api");
		execute(context, api);
	}

	protected abstract void execute(JobExecutionContext context, DiscordApi api) throws JobExecutionException;

	/**
	 * Builder method that produces a {@link JobDetail} for the given job type,
	 * with job data initialized to include a reference to the given Discord API.
	 * @param jobType The type of job to create a job detail for.
	 * @param api The Discord API.
	 * @return The created job detail.
	 */
	public static JobDetail build(Class<? extends DiscordApiJob> jobType, DiscordApi api) {
		return JobBuilder.newJob(jobType)
				.usingJobData(new JobDataMap(Map.of("discord-api", api)))
				.build();
	}
}
