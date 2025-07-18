/*
 * Copyright (c) 2025 esentri AG
 *
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package com.esentri.quartz.carbonaware.clients.opendata;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A Quartz job that periodically updates cached forecast data from OpenData source.
 * This job is scheduled to run multiple times per day at specific hours (8:20, 12:20, 16:20, 18:20, 19:20, 20:20)
 * as defined by {@link #UPDATE_INTERVAL_CRON_PATTERN}.
 *
 * @author jannisschalk
 */
public class OpenDataUpdateJob implements Job {

    private static final Logger LOGGER = LoggerFactory.getLogger(OpenDataUpdateJob.class);

    public static final String JOB_GROUP_NAME = "carbon-aware-scheduler-core";
    public static final String JOB_NAME = "opendata-update-job";
    public static final String UPDATE_INTERVAL_CRON_PATTERN = "0 20 8,12,16,18,19,20 ? * * *";

    /**
     * Executes the job to update cached forecast data.
     * Triggers an update of the cached data in {@link EnergyChartsForecastProvider} and logs the completion.
     *
     * @param context the JobExecutionContext that contains information about the job's execution
     * @throws JobExecutionException if an error occurs during job execution
     */
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        EnergyChartsForecastProvider.updateCachedData();
        LOGGER.info("EnergyChartsForecastProvider update performed");
    }
}
