/*
 * Copyright (c) 2025 esentri AG
 *
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package com.esentri.quartz.carbonaware.plugins;

import com.esentri.quartz.carbonaware.plugins.listeners.CarbonStatisticsTriggerListener;
import com.esentri.quartz.carbonaware.plugins.listeners.TimeShiftingTriggerListener;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.spi.ClassLoadHelper;
import org.quartz.spi.SchedulerPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Plugin to initialize the {@link TimeShiftingTriggerListener} and if configured in
 * quartz.properties the {@link CarbonStatisticsTriggerListener}
 *
 * @author jannisschalk
 * */
public class CarbonAwarePlugin implements SchedulerPlugin {

    private static final Logger LOGGER = LoggerFactory.getLogger(CarbonAwarePlugin.class);

    private String persistenceClientImplementationClass;
    private String restClientImplementationClass;

    private boolean enableStatistics;
    private boolean dryrun;

    @Override
    public void initialize(String name, Scheduler scheduler, ClassLoadHelper loadHelper) throws SchedulerException {
        if (enableStatistics) {
            LOGGER.info("Enabled statistics plugin...");
            scheduler.getListenerManager().addTriggerListener(new CarbonStatisticsTriggerListener(
                    persistenceClientImplementationClass,
                    restClientImplementationClass,
                    dryrun));
        }

        scheduler.getListenerManager().addTriggerListener(new TimeShiftingTriggerListener(dryrun));
    }

    @Override
    public void start() {
        // do nothing
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
}
