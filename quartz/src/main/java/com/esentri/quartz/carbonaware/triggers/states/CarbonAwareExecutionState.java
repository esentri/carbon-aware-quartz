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
