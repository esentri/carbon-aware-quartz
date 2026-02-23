package com.esentri.quartz.carbonaware.springboot;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test verifying that carbon-aware scheduling is dormant by default.
 * <p>
 * Tests verify IT-2: Without {@link EnableCarbonAwareScheduling}, no carbon-aware beans are registered.
 */
@SpringBootTest(classes = DormantByDefaultIT.TestApplication.class)
class DormantByDefaultIT {

    @Test
    void shouldNotActivateExtensionWithoutAnnotation(ApplicationContext context) {
        // IT-2: Dormant by Default (Negative)
        // Verify that without the annotation, no carbon-aware beans are registered
        assertThat(context.getBeansOfType(CarbonAwareSchedulingAutoConfiguration.class))
            .as("CarbonAwareSchedulingAutoConfiguration should NOT exist without annotation")
            .isEmpty();
    }

    @SpringBootApplication
    static class TestApplication {
        // Test application WITHOUT @EnableCarbonAwareScheduling
    }
}
