package com.esentri.quartz.springboot.application;

import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.stereotype.Component;

/**
 * A simple Quartz job implementation that performs logging operations.
 * 
 * <p>This job is designed to be used within a Spring Boot application context
 * and demonstrates basic job execution functionality by logging a message
 * when executed.</p>
 * 
 * <p>The job is automatically managed by Spring's dependency injection
 * container and can be scheduled using Quartz triggers.</p>
 * 
 * @author Carbon-Aware-Quartz Framework
 * @version 1.0
 * @since 1.0
 * 
 * @see org.quartz.Job
 * @see org.springframework.stereotype.Component
 */
@Slf4j
@Component
public class AnotherLoggingJob implements Job {
    
    /**
     * Executes the job logic.
     * 
     * <p>This method is called by the Quartz scheduler when the job is triggered.
     * The implementation logs an informational message indicating that the task
     * has been performed.</p>
     * 
     * <p>The method is designed to be lightweight and performs minimal processing
     * to demonstrate basic job execution capabilities.</p>
     * 
     * @param context the job execution context containing information about the
     *                job's runtime environment, including the scheduler instance,
     *                trigger details, and job data map
     * 
     * @throws JobExecutionException if an error occurs during job execution.
     *                              This exception can be used to instruct the
     *                              scheduler on how to handle the failure
     * 
     * @see JobExecutionContext
     * @see JobExecutionException
     */
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        log.info("My Task was performed...");
    }
}