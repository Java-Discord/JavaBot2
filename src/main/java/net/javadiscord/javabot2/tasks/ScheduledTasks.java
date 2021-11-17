package net.javadiscord.javabot2.tasks;

import net.javadiscord.javabot2.tasks.jobs.DiscordApiJob;
import net.javadiscord.javabot2.tasks.jobs.UnmuteExpiredJob;
import org.javacord.api.DiscordApi;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

/**
 * This class is responsible for setting up all scheduled tasks that the bot
 * should run periodically, using the Quartz {@link Scheduler}. To add new tasks
 * to the schedule, add them to the {@link ScheduledTasks#scheduleAllTasks(Scheduler, DiscordApi)}
 * method.
 */
public class ScheduledTasks {
	// Hide the constructor.
	private ScheduledTasks() {}

	/**
	 * Initializes all scheduled jobs and starts the scheduler. Also adds a
	 * shutdown hook that gracefully stops the scheduler when the program ends.
	 * @param api The Discord API, which may be needed by some jobs.
	 * @throws SchedulerException If an error occurs while starting the scheduler.
	 */
	public static void init(DiscordApi api) throws SchedulerException {
		Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
		scheduleAllTasks(scheduler, api);
		scheduler.start();
		// Add a hook to shut down the scheduler cleanly when the program terminates.
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			try {
				scheduler.shutdown();
			} catch (SchedulerException e) {
				e.printStackTrace();
			}
		}));
	}

	/**
	 * This method is where all tasks are scheduled. <strong>Add scheduled tasks
	 * to the scheduler using this method!</strong>
	 * @param scheduler The scheduler to use.
	 * @param api The Discord API.
	 * @throws SchedulerException If an error occurs while adding a task.
	 */
	private static void scheduleAllTasks(Scheduler scheduler, DiscordApi api) throws SchedulerException {
		scheduleApiJob(scheduler, api, UnmuteExpiredJob.class, SimpleScheduleBuilder.repeatMinutelyForever());
	}

	/**
	 * Convenience method for scheduling an API-dependent job using a single
	 * trigger that follows a given schedule.
	 * @param scheduler The scheduler to add the job to.
	 * @param api The Discord API.
	 * @param type The type of job to schedule.
	 * @param scheduleBuilder A schedule builder that the trigger will use.
	 * @throws SchedulerException If an error occurs while adding the job.
	 * @see SimpleScheduleBuilder
	 * @see CronScheduleBuilder
	 * @see CalendarIntervalScheduleBuilder
	 */
	private static void scheduleApiJob(
			Scheduler scheduler,
			DiscordApi api,
			Class<? extends DiscordApiJob> type,
			ScheduleBuilder<?> scheduleBuilder
	) throws SchedulerException {
		scheduler.scheduleJob(
				DiscordApiJob.build(type, api),
				TriggerBuilder.newTrigger().withSchedule(scheduleBuilder).build()
		);
	}
}
