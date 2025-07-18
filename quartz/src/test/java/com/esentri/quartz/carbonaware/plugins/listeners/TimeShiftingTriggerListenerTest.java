package com.esentri.quartz.carbonaware.plugins.listeners;

import com.esentri.quartz.carbonaware.triggers.impl.CarbonAwareCronTriggerImpl;
import com.esentri.quartz.carbonaware.triggers.states.CarbonAwareExecutionState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.quartz.core.ListenerManagerImpl;
import org.quartz.core.QuartzScheduler;
import org.quartz.impl.triggers.CronTriggerImpl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TimeShiftingTriggerListenerTest {

    @Mock
    private QuartzScheduler quartzScheduler;

    private TimeShiftingTriggerListener sut;

    @BeforeEach
    void setUp() {
        when(quartzScheduler.getListenerManager()).thenReturn(new ListenerManagerImpl());

        sut = new TimeShiftingTriggerListener(false);
    }

    @Test
    void shouldReturnTrue_forJobVeto_IfTriggerStateIs_DeterminedABetterExecutionTime() {
        CarbonAwareCronTriggerImpl trigger = new CarbonAwareCronTriggerImpl();
        trigger.setCarbonAwareTriggerState(CarbonAwareExecutionState.DETERMINED_BETTER_EXECUTION_TIME);

        boolean result = sut.vetoJobExecution(trigger, null);

        assertThat(result).isTrue();
    }

    @Test
    void shouldReturnFalse_forJobVeto_IfTriggerStateIsNot_DeterminedABetterExecutionTime() {
        CarbonAwareCronTriggerImpl trigger = new CarbonAwareCronTriggerImpl();
        trigger.setCarbonAwareTriggerState(CarbonAwareExecutionState.READY);

        boolean result = sut.vetoJobExecution(trigger, null);

        assertThat(result).isFalse();
    }

    @Test
    void shouldReturnFalse_forJobVeto_IfTriggerIsNoCarbonAwareTrigger() {
        CronTriggerImpl trigger = new CronTriggerImpl();

        boolean result = sut.vetoJobExecution(trigger, null);

        assertThat(result).isFalse();
    }

    @Test
    void shouldReturnTrue_forJobVeto_inDryRunMode_IfTriggerStateIs_DeterminedABetterExecutionTime() {
        sut = new TimeShiftingTriggerListener(true);

        CarbonAwareCronTriggerImpl trigger = new CarbonAwareCronTriggerImpl();
        trigger.setCarbonAwareTriggerState(CarbonAwareExecutionState.DETERMINED_BETTER_EXECUTION_TIME);

        boolean result = sut.vetoJobExecution(trigger, null);

        assertThat(result).isFalse();
    }

    @Test
    void shouldReturnFalse_forJobVeto_inDryRunMode_IfTriggerStateIs_isReady() {
        sut = new TimeShiftingTriggerListener(true);

        CarbonAwareCronTriggerImpl trigger = new CarbonAwareCronTriggerImpl();
        trigger.setCarbonAwareTriggerState(CarbonAwareExecutionState.READY);

        boolean result = sut.vetoJobExecution(trigger, null);

        assertThat(result).isTrue();
    }

    @Test
    void shouldReturnFalse_forJobVeto_inDryRunMode_IfTriggerStateIs_isCarbonDataUnavailable() {
        sut = new TimeShiftingTriggerListener(true);

        CarbonAwareCronTriggerImpl trigger = new CarbonAwareCronTriggerImpl();
        trigger.setCarbonAwareTriggerState(CarbonAwareExecutionState.READY);

        boolean result = sut.vetoJobExecution(trigger, null);

        assertThat(result).isTrue();
    }
}