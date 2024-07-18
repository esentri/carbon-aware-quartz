package com.esentri.quartz.springboot.application;

import com.esentri.quartz.carbonaware.triggers.builders.CarbonAwareCronScheduleBuilder;
import com.esentri.quartz.springboot.clients.rest.CarbonForecastClient;
import lombok.RequiredArgsConstructor;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Trigger;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import static org.quartz.TriggerBuilder.newTrigger;

@Component
@RequiredArgsConstructor
public class TriggerConfiguration {

    private final CarbonForecastClient carbonForecastClient;

    @Bean
    public JobDetail jobDetail() {
        return JobBuilder.newJob().ofType(SimpleLoggingJob.class)
                .storeDurably()
                .withIdentity("SimpleLoggingJob", "carbon-aware")
                .withDescription("Invoke Simple logging Job ")
                .build();
    }

    @Bean
    public Trigger trigger(JobDetail job) {
        return newTrigger()
                .withIdentity("CarbonAwareTrigger", "carbon-aware")
                .forJob(job)
                .withSchedule(CarbonAwareCronScheduleBuilder.cronSchedule("0 0/1 * ? * * *")
                        .withJobDurationInMinutes(5)
                        .withDeadlineCronExpression("0 0 23 ? * * *")
                        .withLocation("de")
                        .withCarbonForecastApi(carbonForecastClient))
                .build();
    }


}
