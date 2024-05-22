package com.esentri.quartz.example3;

import com.esentri.quartz.carbonaware.triggers.CarbonAwareCronTrigger;
import com.esentri.quartz.carbonaware.triggers.builders.CarbonAwareCronScheduleBuilder;
import com.esentri.quartz.forecast.client.TestClient;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
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
    private Scheduler scheduler;

    public static void main(String[] args) throws Exception {
        CarbonAwareTriggerStatisticsExample example = new CarbonAwareTriggerStatisticsExample();
        example.run();
    }

    private void run() throws Exception {
        JobDetail carbonDataDownloader = newJob(TimeShiftedJob.class)
                .withIdentity("TimeShiftedJob", "carbon-aware")
                .ofType(TimeShiftedJob.class)
                .build();

        // Carbon Forecast will be determined, but Job will be executed at determined time from cronSchedule(...)
        CarbonAwareCronTrigger carbonAwareTrigger = newTrigger()
                .withIdentity("CarbonAwareTrigger", "carbon-aware")
                .forJob("TimeShiftedJob", "carbon-aware")
                .withSchedule(CarbonAwareCronScheduleBuilder.cronSchedule("20 0/1 * ? * *")
                        .withJobDurationInMinutes(7)
                        .withDeadlineCronExpression("50 0/1 * ? * *")
                        .withLocation("de")
                        .withCarbonForecastApi(new TestClient()))
                .build();

        StdSchedulerFactory sf = new StdSchedulerFactory();
        scheduler = sf.getScheduler();

        scheduler.scheduleJob(carbonDataDownloader, carbonAwareTrigger);
        runScheduler(scheduler);
    }

    private static void runScheduler(Scheduler scheduler) throws Exception {
        LOGGER.info("------- Starting Scheduler -----------------");
        scheduler.start();

        Thread.sleep(90L * 1000L);

        LOGGER.info("------- Shutting Down ---------------------");
        scheduler.shutdown(true);
    }
}
