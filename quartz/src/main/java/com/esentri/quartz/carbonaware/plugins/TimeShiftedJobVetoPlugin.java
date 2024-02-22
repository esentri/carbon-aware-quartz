package com.esentri.quartz.carbonaware.plugins;

import com.esentri.quartz.carbonaware.triggers.CarbonAwareCronTrigger;
import com.esentri.quartz.carbonaware.triggers.states.CarbonAwareExecutionState;
import org.quartz.JobExecutionContext;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.listeners.TriggerListenerSupport;
import org.quartz.spi.ClassLoadHelper;
import org.quartz.spi.SchedulerPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;


public class TimeShiftedJobVetoPlugin extends TriggerListenerSupport implements SchedulerPlugin  {

    private static final Logger LOGGER = LoggerFactory.getLogger(TimeShiftedJobVetoPlugin.class);

    @Override
    public String getName() {
        return getClass().getName();
    }

    @Override
    public void initialize(String name, Scheduler scheduler, ClassLoadHelper loadHelper) throws SchedulerException {
        LOGGER.info("Initialized plugin: {}", getName());
        // initialize and register
        scheduler.getListenerManager().addTriggerListener(this);
    }

    @Override
    public void start() {
        // not required
    }

    @Override
    public void shutdown() {
        // not required
    }


    /**
     * Return true for next job execution, if a better execution time was determined by the {@link CarbonAwareCronTrigger}.
     * In this case, the expected Job is not executed.
     * <br>
     * The trigger will fire again at the determined time, with another {@link CarbonAwareExecutionState}.
     * */
    @Override
    public boolean vetoJobExecution(Trigger trigger, JobExecutionContext context) {
        if (trigger instanceof CarbonAwareCronTrigger carbonAwareTrigger) {
            boolean isVeto = Objects.requireNonNull(carbonAwareTrigger).getTriggerState() == CarbonAwareExecutionState.DETERMINED_BETTER_EXECUTION_TIME;
            LOGGER.info("----- Job veto: {} -----", isVeto);
            return isVeto;
        }
        return false;
    }
}
