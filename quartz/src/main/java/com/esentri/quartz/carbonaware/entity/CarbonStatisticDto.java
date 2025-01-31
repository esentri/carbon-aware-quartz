/*
 * Copyright (c) 2025 esentri AG
 *
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package com.esentri.quartz.carbonaware.entity;

import java.time.Instant;

/**
 * DTO for statistics
 *
 * @author jannisschalk
 * */
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
