package com.esentri.quartz.carbonaware.springboot;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test verifying explicit disable via property.
 * <p>
 * Tests verify IT-3: {@code carbon.aware.scheduling.enabled=false} overrides the annotation.
 */
@SpringBootTest(classes = ExplicitDisableIT.TestApplication.class)
@TestPropertySource(properties = "carbon.aware.scheduling.enabled=false")
class ExplicitDisableIT {

    @Test
    void shouldNotActivateExtensionWhenExplicitlyDisabled(ApplicationContext context) {
        // IT-3: Explicit Disable Toggle
        // Verify that carbon.aware.scheduling.enabled=false overrides the annotation
        assertThat(context.getBeansOfType(CarbonAwareSchedulingAutoConfiguration.class))
            .as("CarbonAwareSchedulingAutoConfiguration should NOT exist when explicitly disabled")
            .isEmpty();
    }

    @SpringBootApplication
    @EnableCarbonAwareScheduling
    static class TestApplication {
        // Test application with annotation but property disabled
    }
}
