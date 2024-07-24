package com.esentri.quartz.carbonaware.plugins.listeners;

import com.esentri.quartz.carbonaware.triggers.CarbonAwareCronTrigger;
import com.esentri.quartz.carbonaware.triggers.states.CarbonAwareExecutionState;
import org.quartz.JobExecutionContext;
import org.quartz.Trigger;
import org.quartz.listeners.TriggerListenerSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * TriggerListener to cancel a {@link org.quartz.Job} execution if a better execution time is determined by a
 * {@link CarbonAwareCronTrigger}
 *
 * @author jannisschalk
 * */
public class TimeShiftingTriggerListener extends TriggerListenerSupport {

    private static final Logger LOGGER = LoggerFactory.getLogger(TimeShiftingTriggerListener.class);

    private boolean dryRun;

    public TimeShiftingTriggerListener(boolean dryRun) {
        this.dryRun = dryRun;
    }

    @Override
    public String getName() {
        return getClass().getName();
    }

    /**
     * Return true for next job execution, if a better execution time was determined by the {@link CarbonAwareCronTrigger}.
     * In this case, the expected Job is not executed.
     * <br>
     * The trigger will fire again at the determined time, with another {@link CarbonAwareExecutionState}.
     * */
    @Override
    public boolean vetoJobExecution(Trigger trigger, JobExecutionContext context) {
        if (dryRun && trigger instanceof CarbonAwareCronTrigger carbonAwareTrigger) {
            boolean isVeto = Objects.requireNonNull(carbonAwareTrigger).getTriggerState() == CarbonAwareExecutionState.DETERMINED_BETTER_EXECUTION_TIME;
            LOGGER.info("----- DRYRUN: Job veto: {} -----", !isVeto);
            return !isVeto;
        }

        if (trigger instanceof CarbonAwareCronTrigger carbonAwareTrigger) {
            boolean isVeto = Objects.requireNonNull(carbonAwareTrigger).getTriggerState() == CarbonAwareExecutionState.DETERMINED_BETTER_EXECUTION_TIME;
            LOGGER.info("----- Job veto: {} -----", isVeto);
            return isVeto;
        }
        return false;
    }
}
