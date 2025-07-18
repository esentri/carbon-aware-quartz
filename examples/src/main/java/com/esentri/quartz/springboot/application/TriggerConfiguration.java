package com.esentri.quartz.springboot.application;

import com.esentri.quartz.carbonaware.triggers.builders.CarbonAwareCronScheduleBuilder;
import com.esentri.quartz.springboot.clients.rest.CarbonForecastClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Trigger;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import static org.quartz.TriggerBuilder.newTrigger;

@Slf4j
@Component
@RequiredArgsConstructor
public class TriggerConfiguration {

    private static final String GROUP_NAME = "carbon-aware";

    private final CarbonForecastClient carbonForecastClient;

    @Bean
    @ConditionalOnProperty(
            name = "spring.quartz.properties.org.quartz.plugin.carbon-aware-plugin.useOpenDataProvider",
            havingValue = "true")
    public JobDetail firstJob() {
        return JobBuilder.newJob().ofType(SimpleLoggingJob.class)
                .storeDurably()
                .withIdentity("OpenDataForecastClient-LoggingJob", GROUP_NAME)
                .withDescription("Invoke Simple logging Job with forecast from default OpenDataForecastClient")
                .build();
    }

    @Bean
    @ConditionalOnProperty(
            name = "spring.quartz.properties.org.quartz.plugin.carbon-aware-plugin.useOpenDataProvider",
            havingValue = "true")
    public Trigger triggerUsingDefaultOpenDataForecastClient(@Qualifier("firstJob") JobDetail job) {

        log.info("Carbon Aware Trigger configured using default OpenDataForecastClient");

        return newTrigger()
                .withIdentity("CarbonAwareTrigger-WithOpenDataClient", GROUP_NAME)
                .forJob(job)
                .withSchedule(CarbonAwareCronScheduleBuilder.cronSchedule("0 0/1 * ? * * *")
                        .withJobDurationInMinutes(49)
                        .withDeadlineCronExpression("0 0 23 ? * * *")
                        .withLocation("de")
                        .useDefaultOpenDataForcastApiClient())
                .build();
    }

    @Bean
    @ConditionalOnProperty(
            name = "spring.quartz.properties.org.quartz.plugin.carbon-aware-plugin.useOpenDataProvider",
            havingValue = "false")
    public JobDetail secondJob() {
        return JobBuilder.newJob().ofType(AnotherLoggingJob.class)
//                .storeDurably()
                .withIdentity("CustomForecastClient-LoggingJob", GROUP_NAME)
                .withDescription("Invoke Simple logging Job with forecast from custom implemented ForecastApi")
                .build();
    }

    @Bean
    @ConditionalOnProperty(
            name = "spring.quartz.properties.org.quartz.plugin.carbon-aware-plugin.useOpenDataProvider",
            havingValue = "false")
    public Trigger triggerUsingCustomImplementedForecastRestClient(@Qualifier("secondJob") JobDetail job) {

        log.info("Carbon Aware Trigger configured using a Custom CarbonForecastApi Implementation");

        return newTrigger()
                .withIdentity("CarbonAwareTrigger-WithCustomForecastClient", GROUP_NAME)
                .forJob(job)
                .withSchedule(CarbonAwareCronScheduleBuilder.cronSchedule("30 0/1 * ? * * *")
                        .withJobDurationInMinutes(5)
                        .withDeadlineCronExpression("0 0 23 ? * * *")
                        .withLocation("de")
                        .withCarbonForecastApi(carbonForecastClient))
                .build();
    }
}
