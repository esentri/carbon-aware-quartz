package com.esentri.quartz.carbonaware.springboot;

import com.esentri.quartz.carbonaware.plugins.listeners.CarbonStatisticsTriggerListener;
import com.esentri.quartz.carbonaware.plugins.listeners.TimeShiftingTriggerListener;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.quartz.Scheduler;
import org.quartz.TriggerListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for Quartz listener attachment.
 * <p>
 * Tests verify IT-4 from the test specification: listeners are correctly attached
 * to the Quartz scheduler based on configuration.
 * <p>
 * Note: IT-4.1 (TimeShiftingTriggerListener) is tested here.
 * IT-4.2 and IT-4.3 (CarbonStatisticsTriggerListener) require Step 4 (Bean Awareness)
 * to be implemented first, as the statistics listener depends on SPI providers.
 */
class QuartzListenerAttachmentIT {

    /**
     * IT-4.1: Verify TimeShiftingTriggerListener is always attached when extension is enabled.
     */
    @Nested
    @SpringBootTest(classes = TimeShiftingListenerTest.TestApplication.class)
    class TimeShiftingListenerTest {

        @Autowired
        private Scheduler scheduler;

        @Test
        void shouldAttachTimeShiftingTriggerListenerWhenEnabled() throws Exception {
            // Verify TimeShiftingTriggerListener is present
            List<TriggerListener> triggerListeners = scheduler.getListenerManager().getTriggerListeners();

            assertThat(triggerListeners)
                .as("TimeShiftingTriggerListener should be attached")
                .anyMatch(listener -> listener instanceof TimeShiftingTriggerListener);

            // Verify it's the only listener (no statistics listener by default)
            assertThat(triggerListeners)
                .as("CarbonStatisticsTriggerListener should NOT be attached by default")
                .noneMatch(listener -> listener instanceof CarbonStatisticsTriggerListener);
        }

        @SpringBootApplication
        @EnableCarbonAwareScheduling
        static class TestApplication {
        }
    }
}
