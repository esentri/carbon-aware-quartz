package com.esentri.quartz.example3;

import com.esentri.quartz.carbonaware.triggers.CarbonAwareCronTrigger;
import com.esentri.quartz.carbonaware.triggers.builders.CarbonAwareCronScheduleBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

/**
 * See Dry-Run configuration in src/main/resources/com/esentri/quartz/example2
 * The Property 'org.quartz.plugin.time-shifted-job-veto-plugin.dryrun' have to be true to enable the feature
 */
public class CarbonAwareTriggerStatisticsExample {

    private static final Logger LOGGER = LoggerFactory.getLogger(CarbonAwareTriggerStatisticsExample.class);
    private static final String GROUP_NAME = "carbon-aware";

    /**
     * The main method serves as the entry point for the application. It creates an instance of
     * {@code CarbonAwareTriggerStatisticsExample} and invokes the {@code run} method to execute
     * the configured carbon-aware scheduling process.
     *
     * @param args the command-line arguments passed to the application
     * @throws Exception if any exception occurs during the execution of the scheduling process
     */
    public static void main(String[] args) throws Exception {
        CarbonAwareTriggerStatisticsExample example = new CarbonAwareTriggerStatisticsExample();
        example.run();
    }

    private void run() throws SchedulerException, InterruptedException {
        JobDetail carbonDataDownloader = newJob(TimeShiftedJob.class)
                .withIdentity("TimeShiftedJob", GROUP_NAME)
                .ofType(TimeShiftedJob.class)
                .build();

        // Carbon Forecast will be determined, but Job will be executed at determined time from cronSchedule(...)
        CarbonAwareCronTrigger carbonAwareTrigger = newTrigger()
                .withIdentity("CarbonAwareTrigger", GROUP_NAME)
                .forJob("TimeShiftedJob", GROUP_NAME)
                .withSchedule(CarbonAwareCronScheduleBuilder.cronSchedule("20 0/1 * ? * *")
                        .withJobDurationInMinutes(7)
                        .withDeadlineCronExpression("50 0/1 * ? * *")
                        .withLocation("de")
                        .useDefaultOpenDataForcastApiClient())
                .build();

        StdSchedulerFactory sf = new StdSchedulerFactory();
        var scheduler = sf.getScheduler();

        scheduler.scheduleJob(carbonDataDownloader, carbonAwareTrigger);
        runScheduler(scheduler);
    }

    private static void runScheduler(Scheduler scheduler) throws SchedulerException, InterruptedException {
        LOGGER.info("------- Starting Scheduler -----------------");
        scheduler.start();

        Thread.sleep(90L * 1000L);

        LOGGER.info("------- Shutting Down ---------------------");
        scheduler.shutdown(true);
    }
}
