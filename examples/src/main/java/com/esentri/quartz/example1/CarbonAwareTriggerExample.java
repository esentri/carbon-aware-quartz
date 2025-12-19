package com.esentri.quartz.example1;

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
 * This class serves as an example implementation for scheduling a job using
 * a CarbonAwareCronTrigger in conjunction with Quartz Scheduler. It highlights
 * the usage of eco-friendly scheduling by leveraging carbon awareness
 * data to determine when to optimally execute a job with minimal carbon
 * emissions.
 * <br>
 * This implementation schedules a sample job (TimeShiftedJob) and utilizes
 * a CarbonAwareCronTrigger to decide the best time to execute the job,
 * factoring in carbon emissions forecasts.
 * <br>
 * The example employs the following components:
 * - A job: TimeShiftedJob, defining the task to be executed.
 * - A trigger: CarbonAwareCronTrigger, which uses scheduling criteria based
 *   on carbon-aware forecasts.
 * - A schedule builder: CarbonAwareCronScheduleBuilder to configure the trigger
 *   with a cron expression, job duration, location, and carbon forecast API.
 * - Quartz Scheduler: Used for scheduling and managing job execution.
 * <br>
 * The CarbonAwareCronTrigger implements eco-conscious scheduling by minimizing
 * the carbon footprint of tasks. This is achieved by analyzing forecasts and
 * favorably scheduling jobs during lower carbon output periods in the power
 * grid of the specified location.
 * <br>
 * The {@code runScheduler} method demonstrates the lifecycle of a Quartz
 * scheduler, including starting, running for a specified duration, and
 * shutdown.
 * <br>
 * Logging is used to provide information about scheduler actions and
 * job execution, enabling easier debugging and monitoring.
 * <br>
 * Exceptions such as {@link SchedulerException} and {@link InterruptedException}
 * are handled to ensure proper error management during scheduling and execution.
 */
public class CarbonAwareTriggerExample {

    private static final Logger LOGGER = LoggerFactory.getLogger(CarbonAwareTriggerExample.class);
    private static final String GROUP_NAME = "carbon-aware";

    /**
     * The entry point of the application which demonstrates the execution
     * of a Carbon-Aware time-shifted job using Quartz scheduler.
     * This method initializes and executes the example.
     *
     * @param args Command-line arguments passed to the application.
     * @throws Exception If an error occurs during job scheduling or execution.
     */
    public static void main(String[] args) throws Exception {
        CarbonAwareTriggerExample example = new CarbonAwareTriggerExample();
        example.run();
    }

    private void run() throws SchedulerException, InterruptedException {
        JobDetail job = newJob(TimeShiftedJob.class)
                .withIdentity("TimeShiftedJob", GROUP_NAME)
                .ofType(TimeShiftedJob.class)
                .build();

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
