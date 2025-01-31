/*
 * Copyright (c) 2025 esentri AG
 *
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package com.esentri.quartz.carbonaware.triggers.states;

/**
 *  @author jannisschalk
 * */
public enum CarbonAwareExecutionState {
    PENDING,

    READY,

    DETERMINED_BETTER_EXECUTION_TIME,

    CARBON_DATA_UNAVAILABLE
}
