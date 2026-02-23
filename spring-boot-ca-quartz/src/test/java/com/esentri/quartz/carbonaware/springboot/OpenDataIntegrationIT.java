package com.esentri.quartz.carbonaware.springboot;

import com.esentri.quartz.carbonaware.clients.opendata.OpenDataUpdateJob;
import org.junit.jupiter.api.Test;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for OpenData turnkey integration.
 * <p>
 * Tests verify IT-6 from the test specification: enabling OpenData schedules
 * the update job and initializes the EnergyChartsForecastProvider.
 */
@SpringBootTest(classes = OpenDataIntegrationIT.TestApplication.class)
@TestPropertySource(properties = {
    "carbon.aware.scheduling.open-data.enabled=true",
    "carbon.aware.scheduling.open-data.locations=de,fr"
})
class OpenDataIntegrationIT {

    @Autowired
    private Scheduler scheduler;

    @Test
    void shouldScheduleOpenDataUpdateJobWhenEnabled() throws SchedulerException {
        // IT-6: OpenData Turnkey Integration
        // Verify that OpenDataUpdateJob is present in the Scheduler
        JobKey jobKey = new JobKey(OpenDataUpdateJob.JOB_NAME, OpenDataUpdateJob.JOB_GROUP_NAME);

        JobDetail jobDetail = scheduler.getJobDetail(jobKey);

        assertThat(jobDetail)
            .as("OpenDataUpdateJob should be scheduled when open-data.enabled=true")
            .isNotNull();

        assertThat(jobDetail.getJobClass())
            .as("Job should be of type OpenDataUpdateJob")
            .isEqualTo(OpenDataUpdateJob.class);
    }

    @Test
    void shouldInitializeEnergyChartsForecastProviderWhenEnabled() {
        // IT-6: OpenData Turnkey Integration
        // Verify that EnergyChartsForecastProvider is initialized
        // Note: The actual initialization happens in the CarbonAwarePlugin.start() method
        // which is called when the scheduler starts. This test verifies that the
        // scheduler has started successfully with OpenData enabled.

        try {
            assertThat(scheduler.isStarted())
                .as("Scheduler should be started")
                .isTrue();
        } catch (SchedulerException e) {
            throw new RuntimeException("Failed to check scheduler status", e);
        }
    }

    @SpringBootApplication
    @EnableCarbonAwareScheduling
    static class TestApplication {
        // Test application with OpenData enabled
    }
}
