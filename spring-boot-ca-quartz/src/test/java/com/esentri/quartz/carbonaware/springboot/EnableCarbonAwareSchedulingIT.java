package com.esentri.quartz.carbonaware.springboot;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link EnableCarbonAwareScheduling} annotation.
 * <p>
 * Tests verify the opt-in activation mechanism as specified in IT-1.
 */
@SpringBootTest(classes = EnableCarbonAwareSchedulingIT.TestApplication.class)
class EnableCarbonAwareSchedulingIT {

    @Test
    void shouldActivateExtensionWhenAnnotationPresent(ApplicationContext context) {
        // IT-1: Opt-In Activation (Positive)
        // Verify that @EnableCarbonAwareScheduling activates the extension
        assertThat(context.getBeansOfType(CarbonAwareSchedulingAutoConfiguration.class))
            .as("CarbonAwareSchedulingAutoConfiguration bean should exist")
            .isNotEmpty();

        assertThat(context.getBean(CarbonAwareSchedulingAutoConfiguration.class))
            .as("CarbonAwareSchedulingAutoConfiguration should be instantiated")
            .isNotNull();

        // Verify that CarbonAwareProperties bean exists
        assertThat(context.getBeansOfType(CarbonAwareProperties.class))
            .as("CarbonAwareProperties bean should exist")
            .isNotEmpty();

        // Verify that CarbonAwareSchedulerCustomizer bean exists
        assertThat(context.getBeansOfType(CarbonAwareSchedulerCustomizer.class))
            .as("CarbonAwareSchedulerCustomizer bean should exist")
            .isNotEmpty();
    }

    @SpringBootApplication
    @EnableCarbonAwareScheduling
    static class TestApplication {
        // Test application with annotation enabled
    }
}
