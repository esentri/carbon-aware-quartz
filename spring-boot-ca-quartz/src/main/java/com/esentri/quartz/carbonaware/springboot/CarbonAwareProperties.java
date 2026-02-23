package com.esentri.quartz.carbonaware.springboot;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * Type-safe configuration properties for carbon-aware scheduling.
 * <p>
 * Properties are bound from the {@code carbon.aware.scheduling} prefix.
 * Provides centralized configuration for enabling the extension, controlling
 * dry-run mode, statistics collection, and forecast provider settings.
 * <p>
 * <b>god-mode enabled:</b> All properties support IDE autocomplete via
 * {@code spring-boot-configuration-processor}.
 *
 * @see EnableCarbonAwareScheduling
 */
@ConfigurationProperties(prefix = "carbon.aware.scheduling")
public class CarbonAwareProperties {

    /**
     * Master toggle to enable or disable carbon-aware scheduling.
     * <p>
     * When {@code false}, the extension remains dormant even if
     * {@link EnableCarbonAwareScheduling} is present.
     * <p>
     * Default: {@code true}
     */
    private boolean enabled = true;

    /**
     * Enables dry-run mode for simulated scheduling.
     * <p>
     * When {@code true}, triggers are not actually rescheduled, but the
     * time-shifting logic is still executed and logged.
     * <p>
     * Default: {@code false}
     */
    private boolean dryRun = false;

    /**
     * Statistics configuration for carbon intensity recording.
     */
    private Statistics statistics = new Statistics();

    /**
     * OpenData forecast provider configuration.
     */
    private OpenData openData = new OpenData();

    /**
     * Forecast API configuration and overrides.
     */
    private Forecast forecast = new Forecast();

    /**
     * Persistence API configuration and overrides.
     */
    private Persistence persistence = new Persistence();

    /**
     * Returns whether carbon-aware scheduling is enabled.
     *
     * @return true if enabled
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Sets whether carbon-aware scheduling is enabled.
     *
     * @param enabled true to enable
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Returns whether dry-run mode is enabled.
     *
     * @return true if dry-run mode is enabled
     */
    public boolean isDryRun() {
        return dryRun;
    }

    /**
     * Sets whether dry-run mode is enabled.
     *
     * @param dryRun true to enable dry-run mode
     */
    public void setDryRun(boolean dryRun) {
        this.dryRun = dryRun;
    }

    /**
     * Returns the statistics configuration.
     *
     * @return the statistics configuration
     */
    public Statistics getStatistics() {
        return statistics;
    }

    /**
     * Sets the statistics configuration.
     *
     * @param statistics the statistics configuration
     */
    public void setStatistics(Statistics statistics) {
        this.statistics = statistics;
    }

    /**
     * Returns the OpenData provider configuration.
     *
     * @return the OpenData configuration
     */
    public OpenData getOpenData() {
        return openData;
    }

    /**
     * Sets the OpenData provider configuration.
     *
     * @param openData the OpenData configuration
     */
    public void setOpenData(OpenData openData) {
        this.openData = openData;
    }

    /**
     * Returns the forecast API configuration.
     *
     * @return the forecast configuration
     */
    public Forecast getForecast() {
        return forecast;
    }

    /**
     * Sets the forecast API configuration.
     *
     * @param forecast the forecast configuration
     */
    public void setForecast(Forecast forecast) {
        this.forecast = forecast;
    }

    /**
     * Returns the persistence API configuration.
     *
     * @return the persistence configuration
     */
    public Persistence getPersistence() {
        return persistence;
    }

    /**
     * Sets the persistence API configuration.
     *
     * @param persistence the persistence configuration
     */
    public void setPersistence(Persistence persistence) {
        this.persistence = persistence;
    }

    /**
     * Statistics configuration.
     */
    public static class Statistics {

        /**
         * Enables recording of carbon intensity statistics.
         * <p>
         * When {@code true}, the {@code CarbonStatisticsTriggerListener} is attached
         * to the Quartz scheduler to track carbon intensity data.
         * <p>
         * Default: {@code false}
         */
        private boolean enabled = false;

        /**
         * Returns whether statistics collection is enabled.
         *
         * @return true if statistics are enabled
         */
        public boolean isEnabled() {
            return enabled;
        }

        /**
         * Sets whether statistics collection is enabled.
         *
         * @param enabled true to enable statistics
         */
        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }

    /**
     * OpenData forecast provider configuration.
     */
    public static class OpenData {

        /**
         * Enables the built-in OpenData (EnergyCharts) forecast provider.
         * <p>
         * When {@code true}, the provider is initialized and the update job is scheduled.
         * <p>
         * Default: {@code false}
         */
        private boolean enabled = false;

        /**
         * List of location identifiers for the OpenData forecast provider.
         * <p>
         * Locations must match the format expected by the EnergyCharts API.
         * <p>
         * Default: empty list
         */
        private List<String> locations = new ArrayList<>();

        /**
         * Returns whether the OpenData provider is enabled.
         *
         * @return true if OpenData provider is enabled
         */
        public boolean isEnabled() {
            return enabled;
        }

        /**
         * Sets whether the OpenData provider is enabled.
         *
         * @param enabled true to enable OpenData provider
         */
        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        /**
         * Returns the list of location identifiers.
         *
         * @return the locations list
         */
        public List<String> getLocations() {
            return locations;
        }

        /**
         * Sets the list of location identifiers.
         *
         * @param locations the locations list
         */
        public void setLocations(List<String> locations) {
            this.locations = locations;
        }
    }

    /**
     * Forecast API configuration and bean selection overrides.
     */
    public static class Forecast {

        /**
         * Explicit Spring bean name to use for {@code CarbonForecastApi}.
         * <p>
         * When specified, this bean takes precedence over {@code @Primary}
         * and automatic bean selection.
         * <p>
         * Optional.
         */
        private String beanName;

        /**
         * Fully-qualified class name to instantiate for {@code CarbonForecastApi}.
         * <p>
         * Used when no Spring bean is desired; bridges to SPI-like behavior.
         * The class must have a no-args constructor.
         * <p>
         * Optional.
         */
        private String implClass;

        /**
         * Returns the forecast bean name override.
         *
         * @return the bean name, or null
         */
        public String getBeanName() {
            return beanName;
        }

        /**
         * Sets the forecast bean name override.
         *
         * @param beanName the bean name
         */
        public void setBeanName(String beanName) {
            this.beanName = beanName;
        }

        /**
         * Returns the forecast implementation class name.
         *
         * @return the implementation class name, or null
         */
        public String getImplClass() {
            return implClass;
        }

        /**
         * Sets the forecast implementation class name.
         *
         * @param implClass the implementation class name
         */
        public void setImplClass(String implClass) {
            this.implClass = implClass;
        }
    }

    /**
     * Persistence API configuration and bean selection overrides.
     */
    public static class Persistence {

        /**
         * Explicit Spring bean name to use for {@code PersistenceApi}.
         * <p>
         * When specified, this bean takes precedence over {@code @Primary}
         * and automatic bean selection.
         * <p>
         * Optional.
         */
        private String beanName;

        /**
         * Fully-qualified class name to instantiate for {@code PersistenceApi}.
         * <p>
         * Used when no Spring bean is desired; bridges to SPI-like behavior.
         * The class must have a no-args constructor.
         * <p>
         * Optional.
         */
        private String implClass;

        /**
         * Returns the persistence bean name override.
         *
         * @return the bean name, or null
         */
        public String getBeanName() {
            return beanName;
        }

        /**
         * Sets the persistence bean name override.
         *
         * @param beanName the bean name
         */
        public void setBeanName(String beanName) {
            this.beanName = beanName;
        }

        /**
         * Returns the persistence implementation class name.
         *
         * @return the implementation class name, or null
         */
        public String getImplClass() {
            return implClass;
        }

        /**
         * Sets the persistence implementation class name.
         *
         * @param implClass the implementation class name
         */
        public void setImplClass(String implClass) {
            this.implClass = implClass;
        }
    }
}
