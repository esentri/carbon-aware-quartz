package com.esentri.quartz.carbonaware.entity;

import java.time.Instant;

public class Persistable {

    private String jobExecutionId;
    private String jobName;
    private String jobGroupName;
    private Instant configuredTimestamp;
    private Instant executionTimestamp;
    private Integer jobDuration;
    private Double actualCarbonIntensity;
    private Double optimalCarbonIntensity;
    private String location;
    private Boolean dryRun;

    public Persistable(
            String jobExecutionId,
            String jobName,
            String jobGroupName,
            Instant configuredTimestamp,
            Instant executionTimestamp,
            Integer jobDuration,
            Double actualCarbonIntensity,
            Double optimalCarbonIntensity,
            String location,
            Boolean dryRun) {

        this.jobExecutionId = jobExecutionId;
        this.jobName = jobName;
        this.jobGroupName = jobGroupName;
        this.configuredTimestamp = configuredTimestamp;
        this.executionTimestamp = executionTimestamp;
        this.jobDuration = jobDuration;
        this.actualCarbonIntensity = actualCarbonIntensity;
        this.optimalCarbonIntensity = optimalCarbonIntensity;
        this.location = location;
        this.dryRun = dryRun;
    }

    public String getJobExecutionId() {
        return jobExecutionId;
    }

    public void setJobExecutionId(String jobExecutionId) {
        this.jobExecutionId = jobExecutionId;
    }

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public String getJobGroupName() {
        return jobGroupName;
    }

    public void setJobGroupName(String jobGroupName) {
        this.jobGroupName = jobGroupName;
    }

    public Instant getConfiguredTimestamp() {
        return configuredTimestamp;
    }

    public void setConfiguredTimestamp(Instant configuredTimestamp) {
        this.configuredTimestamp = configuredTimestamp;
    }

    public Instant getExecutionTimestamp() {
        return executionTimestamp;
    }

    public void setExecutionTimestamp(Instant executionTimestamp) {
        this.executionTimestamp = executionTimestamp;
    }

    public Integer getJobDuration() {
        return jobDuration;
    }

    public void setJobDuration(Integer jobDuration) {
        this.jobDuration = jobDuration;
    }

    public Double getActualCarbonIntensity() {
        return actualCarbonIntensity;
    }

    public void setActualCarbonIntensity(Double actualCarbonIntensity) {
        this.actualCarbonIntensity = actualCarbonIntensity;
    }

    public Double getOptimalCarbonIntensity() {
        return optimalCarbonIntensity;
    }

    public void setOptimalCarbonIntensity(Double optimalCarbonIntensity) {
        this.optimalCarbonIntensity = optimalCarbonIntensity;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Boolean getDryRun() {
        return dryRun;
    }

    public void setDryRun(Boolean dryRun) {
        this.dryRun = dryRun;
    }
}
