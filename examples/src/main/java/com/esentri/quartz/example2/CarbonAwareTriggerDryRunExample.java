package com.esentri.quartz.example2;

import com.esentri.quartz.carbonaware.triggers.CarbonAwareCronTrigger;
import com.esentri.quartz.carbonaware.triggers.builders.CarbonAwareCronScheduleBuilder;
import com.esentri.quartz.shared.clients.forecast.TestForecastClient;
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
 * */
public class CarbonAwareTriggerDryRunExample {

    private static final Logger LOGGER = LoggerFactory.getLogger(CarbonAwareTriggerDryRunExample.class);
    private static final String GROUP_NAME = "carbon-aware";

    /**
     * The entry point of the application. Initiates and runs the example demonstrating
     * the usage of a time-shifted job with CarbonAwareTrigger in dry-run mode.
     *
     * @param args command-line arguments
     * @throws Exception if an unexpected error occurs during execution
     */
    public static void main(String[] args) throws Exception {
        CarbonAwareTriggerDryRunExample example = new CarbonAwareTriggerDryRunExample();
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
                        .withCarbonForecastApi(new TestForecastClient()))
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
