package com.esentri.quartz.example2;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TimeShiftedJob implements Job {

    private static final Logger LOGGER = LoggerFactory.getLogger(TimeShiftedJob.class);


    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        LOGGER.info("Job ob will be executed...");
    }
}
