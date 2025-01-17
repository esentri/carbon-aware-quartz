package com.esentri.quartz.example1;

import com.esentri.quartz.carbonaware.triggers.CarbonAwareCronTrigger;
import com.esentri.quartz.forecast.client.TestClient;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import com.esentri.quartz.carbonaware.triggers.builders.CarbonAwareCronScheduleBuilder;
import org.quartz.SchedulerException;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

public class CarbonAwareTriggerExample {

    private static final Logger LOGGER = LoggerFactory.getLogger(CarbonAwareTriggerExample.class);

    public static void main(String[] args) throws Exception {
        CarbonAwareTriggerExample example = new CarbonAwareTriggerExample();
        example.run();
    }

    private void run() throws SchedulerException, InterruptedException {
        JobDetail job = newJob(TimeShiftedJob.class)
                .withIdentity("TimeShiftedJob", "carbon-aware")
                .ofType(TimeShiftedJob.class)
                .build();

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
        var scheduler = sf.getScheduler();

        scheduler.scheduleJob(job, carbonAwareTrigger);
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
