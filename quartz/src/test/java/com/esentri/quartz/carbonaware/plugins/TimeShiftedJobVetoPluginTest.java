package com.esentri.quartz.carbonaware.plugins;

import com.esentri.quartz.carbonaware.triggers.impl.CarbonAwareCronTriggerImpl;
import com.esentri.quartz.carbonaware.triggers.states.CarbonAwareExecutionState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.quartz.TriggerListener;
import org.quartz.core.ListenerManagerImpl;
import org.quartz.core.QuartzScheduler;
import org.quartz.impl.StdScheduler;
import org.quartz.impl.triggers.CronTriggerImpl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TimeShiftedJobVetoPluginTest {

    @Mock
    private QuartzScheduler quartzScheduler;

    private TimeShiftedJobVetoPlugin sut;

    @BeforeEach
    void setUp() {
        when(quartzScheduler.getListenerManager()).thenReturn(new ListenerManagerImpl());

        sut = new TimeShiftedJobVetoPlugin();
    }

    @Test
    void shouldRegisterItself() throws Exception {
        StdScheduler scheduler = new StdScheduler(quartzScheduler);

        sut.initialize("test", scheduler, null);

        assertThat(scheduler.getListenerManager().getTriggerListeners())
                .singleElement()
                .extracting(TriggerListener::getName)
                .isEqualTo("com.esentri.quartz.carbonaware.plugins.TimeShiftedJobVetoPlugin");
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

}