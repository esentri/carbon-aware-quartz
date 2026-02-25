package com.esentri.quartz.carbonaware.springboot;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link CarbonAwareProperties}.
 * <p>
 * Tests verify UT-1 (property binding) and UT-2 (default values) from the test specification.
 */
class CarbonAwarePropertiesTest {

    /**
     * UT-1: Property Binding
     * <p>
     * Verifies that properties under {@code carbon.aware.scheduling} are correctly
     * bound to the {@link CarbonAwareProperties} bean.
     */
    @Nested
    @SpringBootTest(classes = PropertyBindingTest.TestApplication.class)
    @TestPropertySource(properties = {
        "carbon.aware.scheduling.dry-run=true",
        "carbon.aware.scheduling.open-data.locations=DE,FR,NL"
    })
    class PropertyBindingTest {

        @Autowired
        private CarbonAwareProperties properties;

        @Test
        void shouldBindAllPropertiesCorrectly() {
            // Verify global toggles (enabled defaults to true when not set)
            assertThat(properties.isEnabled())
                .as("enabled property should be bound (defaults to true)")
                .isTrue();

            assertThat(properties.isDryRun())
                .as("dry-run property should be bound")
                .isTrue();

            // Verify statistics configuration (defaults to false, not set in properties)
            assertThat(properties.getStatistics().isEnabled())
                .as("statistics.enabled should default to false")
                .isFalse();

            // Verify open-data configuration (defaults to false, not set in properties)
            assertThat(properties.getOpenData().isEnabled())
                .as("open-data.enabled should default to false")
                .isFalse();

            assertThat(properties.getOpenData().getLocations())
                .as("open-data.locations property should be bound")
                .containsExactly("DE", "FR", "NL");

            // Verify forecast configuration (defaults)
            assertThat(properties.getForecast().getBeanName())
                .as("forecast.bean-name should default to null")
                .isNull();

            assertThat(properties.getForecast().getImplClass())
                .as("forecast.impl-class should default to null")
                .isNull();

            // Verify persistence configuration (defaults)
            assertThat(properties.getPersistence().getBeanName())
                .as("persistence.bean-name should default to null")
                .isNull();

            assertThat(properties.getPersistence().getImplClass())
                .as("persistence.impl-class should default to null")
                .isNull();
        }

        @SpringBootApplication
        @EnableCarbonAwareScheduling
        static class TestApplication {
        }
    }

    /**
     * UT-2: Default Values
     * <p>
     * Ensures that default values are correctly applied when properties are missing.
     */
    @Nested
    @SpringBootTest(classes = DefaultValuesTest.TestApplication.class)
    class DefaultValuesTest {

        @Autowired
        private CarbonAwareProperties properties;

        @Test
        void shouldApplyDefaultValuesWhenPropertiesAreMissing() {
            // Verify default global toggles
            assertThat(properties.isEnabled())
                .as("enabled should default to true")
                .isTrue();

            assertThat(properties.isDryRun())
                .as("dry-run should default to false")
                .isFalse();

            // Verify default statistics configuration
            assertThat(properties.getStatistics().isEnabled())
                .as("statistics.enabled should default to false")
                .isFalse();

            // Verify default open-data configuration
            assertThat(properties.getOpenData().isEnabled())
                .as("open-data.enabled should default to false")
                .isFalse();

            assertThat(properties.getOpenData().getLocations())
                .as("open-data.locations should default to empty list")
                .isEmpty();

            // Verify default forecast configuration
            assertThat(properties.getForecast().getBeanName())
                .as("forecast.bean-name should default to null")
                .isNull();

            assertThat(properties.getForecast().getImplClass())
                .as("forecast.impl-class should default to null")
                .isNull();

            // Verify default persistence configuration
            assertThat(properties.getPersistence().getBeanName())
                .as("persistence.bean-name should default to null")
                .isNull();

            assertThat(properties.getPersistence().getImplClass())
                .as("persistence.impl-class should default to null")
                .isNull();
        }

        @SpringBootApplication
        @EnableCarbonAwareScheduling
        static class TestApplication {
        }
    }
}
