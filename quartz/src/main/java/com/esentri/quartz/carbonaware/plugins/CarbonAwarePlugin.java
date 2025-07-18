/*
 * Copyright (c) 2025 esentri AG
 *
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package com.esentri.quartz.carbonaware.plugins;

import com.esentri.quartz.carbonaware.clients.opendata.EnergyChartsForecastProvider;
import com.esentri.quartz.carbonaware.clients.opendata.OpenDataUpdateJob;
import com.esentri.quartz.carbonaware.plugins.listeners.CarbonStatisticsTriggerListener;
import com.esentri.quartz.carbonaware.plugins.listeners.TimeShiftingTriggerListener;
import org.quartz.*;
import org.quartz.spi.ClassLoadHelper;
import org.quartz.spi.SchedulerPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

/**
 * Plugin to initialize the {@link TimeShiftingTriggerListener} and if configured in
 * quartz.properties the {@link CarbonStatisticsTriggerListener}
 * If configured using the OpenData Provider, the OpenDataProvider will be initialized with the configured locations
 * from quartz.properties.
 * Also, the {@link OpenDataUpdateJob} will be scheduled to ensure the cached forecast is up to date
 * @author jannisschalk
 * */
public class CarbonAwarePlugin implements SchedulerPlugin {

    private static final Logger LOGGER = LoggerFactory.getLogger(CarbonAwarePlugin.class);

    private String persistenceClientImplementationClass;
    private String restClientImplementationClass;
    private String openDataLocations;

    private boolean enableStatistics;
    private boolean dryrun;
    private boolean useOpenDataProvider;

    private Runnable openDataUpdateJobRegisterer;

    @Override
    public void initialize(String name, Scheduler scheduler, ClassLoadHelper loadHelper) throws SchedulerException {
        initOpenDataProviderJobRegistratorIfConfigured(scheduler);
        initCarbonStatisticsTriggerListenerIfConfigured(scheduler);

        scheduler.getListenerManager().addTriggerListener(new TimeShiftingTriggerListener(dryrun));
    }

    private void initCarbonStatisticsTriggerListenerIfConfigured(Scheduler scheduler) throws SchedulerException {
        if (enableStatistics) {
            LOGGER.info("Enabled statistics plugin...");
            scheduler.getListenerManager().addTriggerListener(new CarbonStatisticsTriggerListener(
                    persistenceClientImplementationClass,
                    restClientImplementationClass,
                    dryrun));
        }
    }

    private void initOpenDataProviderJobRegistratorIfConfigured(Scheduler scheduler) {
        if (useOpenDataProvider) {
            LOGGER.info("Enabled Default OpenDataProvider ...");
            openDataUpdateJobRegisterer = () -> {
                List<String> locations = Arrays.asList(openDataLocations.split(","));
                EnergyChartsForecastProvider.initialize(locations);

                JobDetail openDataUpdateJob = newJob(OpenDataUpdateJob.class)
                        .withIdentity(OpenDataUpdateJob.JOB_NAME, OpenDataUpdateJob.JOB_GROUP_NAME)
                        .build();
                CronTrigger openDataUpdateTrigger = newTrigger()
                        .withIdentity("OpenDataProviderUpdateTrigger", OpenDataUpdateJob.JOB_GROUP_NAME)
                        .forJob(OpenDataUpdateJob.JOB_NAME, OpenDataUpdateJob.JOB_GROUP_NAME)
                        .withSchedule(CronScheduleBuilder.cronSchedule(OpenDataUpdateJob.UPDATE_INTERVAL_CRON_PATTERN))
                        .build();
                try {
                    scheduler.scheduleJob(openDataUpdateJob, Set.of(openDataUpdateTrigger), true);
                } catch (SchedulerException e) {
                    throw new IllegalStateException(
                            "Scheduler is not able to schedule the %s task."
                                    .formatted(OpenDataUpdateJob.class.getName()), e);
                }
            };
        }
    }

    @Override
    public void start() {
        // start the update job when scheduler is started
        if (useOpenDataProvider) {
            openDataUpdateJobRegisterer.run();
        }
    }

    @Override
    public void shutdown() {
        // do nothing
    }

    public String getPersistenceClientImplementationClass() {
        return persistenceClientImplementationClass;
    }

    public void setPersistenceClientImplementationClass(String persistenceClientImplementationClass) {
        this.persistenceClientImplementationClass = persistenceClientImplementationClass;
    }

    public String getRestClientImplementationClass() {
        return restClientImplementationClass;
    }

    public void setRestClientImplementationClass(String restClientImplementationClass) {
        this.restClientImplementationClass = restClientImplementationClass;
    }

    public boolean isEnableStatistics() {
        return enableStatistics;
    }

    public void setEnableStatistics(boolean enableStatistics) {
        this.enableStatistics = enableStatistics;
    }

    public boolean isDryrun() {
        return dryrun;
    }

    public void setDryrun(boolean dryrun) {
        this.dryrun = dryrun;
    }

    public String getOpenDataLocations() {
        return openDataLocations;
    }

    public void setOpenDataLocations(String openDataLocations) {
        this.openDataLocations = openDataLocations;
    }

    public boolean isUseOpenDataProvider() {
        return useOpenDataProvider;
    }

    public void setUseOpenDataProvider(boolean useOpenDataProvider) {
        this.useOpenDataProvider = useOpenDataProvider;
    }
}
