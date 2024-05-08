package com.esentri.quartz.carbonaware.entity;

import java.time.Instant;

public record CarbonStatisticDto(String jobExecutionId,
                                 String jobName,
                                 String jobGroupName,
                                 Instant configuredTimestamp,
                                 Instant executionTimestamp,
                                 Integer jobDuration,
                                 Double carbonIntensityForConfiguredTimestamp,
                                 Double carbonIntensityForRescheduledTimestamp,
                                 String location,
                                 Boolean dryRun) {

}
