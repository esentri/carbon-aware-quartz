package com.esentri.quartz.carbonaware.springboot;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Auto-configuration for carbon-aware scheduling with Quartz.
 * <p>
 * This configuration is activated when:
 * <ul>
 *   <li>The {@link EnableCarbonAwareScheduling} annotation is present on a configuration class</li>
 *   <li>The property {@code carbon.aware.scheduling.enabled} is {@code true} (default)</li>
 * </ul>
 * <p>
 * If the annotation is present but the property is {@code false}, the extension remains dormant.
 * <p>
 * <b>god-mode enabled:</b> Registers {@link CarbonAwareProperties} for type-safe configuration,
 * {@link ApiProviderResolver} for smart bean detection, and {@link CarbonAwareSchedulerCustomizer}
 * to integrate with Quartz.
 */
@AutoConfiguration
@Configuration
@ConditionalOnProperty(
    name = "carbon.aware.scheduling.enabled",
    havingValue = "true",
    matchIfMissing = true
)
@EnableConfigurationProperties(CarbonAwareProperties.class)
public class CarbonAwareSchedulingAutoConfiguration {

    /**
     * Creates the API provider resolver for detecting and selecting Spring beans.
     *
     * @param applicationContext the Spring application context
     * @param properties the carbon-aware configuration properties
     * @return the API provider resolver
     */
    @Bean
    public ApiProviderResolver apiProviderResolver(
            ApplicationContext applicationContext,
            CarbonAwareProperties properties) {
        return new ApiProviderResolver(applicationContext, properties);
    }

    /**
     * Creates the scheduler customizer that registers the CarbonAwarePlugin
     * and configures the Quartz scheduler with carbon-aware capabilities.
     *
     * @param properties the carbon-aware configuration properties
     * @param providerResolver the API provider resolver
     * @return the scheduler customizer
     */
    @Bean
    public CarbonAwareSchedulerCustomizer carbonAwareSchedulerCustomizer(
            CarbonAwareProperties properties,
            ApiProviderResolver providerResolver) {
        return new CarbonAwareSchedulerCustomizer(properties, providerResolver);
    }

}
