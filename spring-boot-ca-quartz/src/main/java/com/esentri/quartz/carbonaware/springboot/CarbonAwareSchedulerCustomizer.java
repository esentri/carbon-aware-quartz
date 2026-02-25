package com.esentri.quartz.carbonaware.springboot;

import com.esentri.quartz.carbonaware.clients.persistence.PersistenceApi;
import com.esentri.quartz.carbonaware.clients.rest.CarbonForecastApi;
import com.esentri.quartz.carbonaware.plugins.CarbonAwarePlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.quartz.SchedulerFactoryBeanCustomizer;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

import java.util.Properties;
import java.util.stream.Collectors;

/**
 * Customizes the Quartz {@link SchedulerFactoryBean} to register the {@link CarbonAwarePlugin}
 * and configure it with properties from {@link CarbonAwareProperties}.
 * <p>
 * <b>god-mode enabled:</b> This customizer ensures that:
 * <ul>
 *   <li>{@code TimeShiftingTriggerListener} is always attached when the extension is active</li>
 *   <li>{@code CarbonStatisticsTriggerListener} is conditionally attached based on {@code statistics.enabled}</li>
 *   <li>The {@code dry-run} property is propagated to both listeners</li>
 *   <li>OpenData provider is initialized if {@code open-data.enabled} is true</li>
 *   <li>Spring beans for {@code CarbonForecastApi} and {@code PersistenceApi} are detected and bridged</li>
 * </ul>
 *
 * @see CarbonAwarePlugin
 * @see CarbonAwareProperties
 * @see ApiProviderResolver
 */
public class CarbonAwareSchedulerCustomizer implements SchedulerFactoryBeanCustomizer {

    private static final Logger LOGGER = LoggerFactory.getLogger(CarbonAwareSchedulerCustomizer.class);
    private static final String PLUGIN_PREFIX = "org.quartz.plugin.carbon-aware-plugin";

    private final CarbonAwareProperties properties;
    private final ApiProviderResolver providerResolver;

    /**
     * Constructs a new scheduler customizer.
     *
     * @param properties the carbon-aware configuration properties
     * @param providerResolver the API provider resolver
     */
    public CarbonAwareSchedulerCustomizer(CarbonAwareProperties properties, ApiProviderResolver providerResolver) {
        this.properties = properties;
        this.providerResolver = providerResolver;
    }

    @Override
    public void customize(SchedulerFactoryBean schedulerFactoryBean) {
        // Resolve and register Spring beans for API providers
        resolveAndRegisterProviders();

        Properties quartzProperties = new Properties();

        // Register the CarbonAwarePlugin
        quartzProperties.setProperty(PLUGIN_PREFIX + ".class", CarbonAwarePlugin.class.getName());

        // Propagate dry-run property to the plugin
        quartzProperties.setProperty(PLUGIN_PREFIX + ".dryrun", String.valueOf(properties.isDryRun()));

        // Configure statistics listener (conditional)
        quartzProperties.setProperty(
            PLUGIN_PREFIX + ".enableStatistics",
            String.valueOf(properties.getStatistics().isEnabled())
        );

        // Resolve and configure CarbonForecastApi provider
        ApiProviderResolver.ProviderResolutionResult<CarbonForecastApi> forecastResult =
            providerResolver.resolveForecastProvider(CarbonForecastApi.class);

        if (forecastResult.isSpringBeanResolved()) {
            // Use Spring bean wrapper for SPI
            LOGGER.info("CarbonForecastApi resolved via Spring: {}", forecastResult.getResolutionMethod());
            quartzProperties.setProperty(
                PLUGIN_PREFIX + ".restClientImplementationClass",
                SpringBeanCarbonForecastApiWrapper.class.getSimpleName()
            );
        } else if (forecastResult.getImplementationClass() != null) {
            // Use property-specified impl-class
            LOGGER.info("CarbonForecastApi resolved via impl-class: {}", forecastResult.getImplementationClass());
            quartzProperties.setProperty(
                PLUGIN_PREFIX + ".restClientImplementationClass",
                forecastResult.getImplementationClass()
            );
        } else {
            LOGGER.info("CarbonForecastApi using SPI fallback");
        }

        // Resolve and configure PersistenceApi provider
        ApiProviderResolver.ProviderResolutionResult<PersistenceApi> persistenceResult =
            providerResolver.resolvePersistenceProvider(PersistenceApi.class);

        if (persistenceResult.isSpringBeanResolved()) {
            // Use Spring bean wrapper for SPI
            LOGGER.info("PersistenceApi resolved via Spring: {}", persistenceResult.getResolutionMethod());
            quartzProperties.setProperty(
                PLUGIN_PREFIX + ".persistenceClientImplementationClass",
                SpringBeanPersistenceApiWrapper.class.getSimpleName()
            );
        } else if (persistenceResult.getImplementationClass() != null) {
            // Use property-specified impl-class
            LOGGER.info("PersistenceApi resolved via impl-class: {}", persistenceResult.getImplementationClass());
            quartzProperties.setProperty(
                PLUGIN_PREFIX + ".persistenceClientImplementationClass",
                persistenceResult.getImplementationClass()
            );
        } else {
            LOGGER.info("PersistenceApi using SPI fallback");
        }

        // Configure OpenData provider if enabled
        if (properties.getOpenData().isEnabled()) {
            quartzProperties.setProperty(
                PLUGIN_PREFIX + ".useOpenDataProvider",
                "true"
            );

            // Set OpenData locations (comma-separated)
            if (!properties.getOpenData().getLocations().isEmpty()) {
                String locations = properties.getOpenData().getLocations().stream()
                    .collect(Collectors.joining(","));
                quartzProperties.setProperty(
                    PLUGIN_PREFIX + ".openDataLocations",
                    locations
                );
            }
        }

        // Set the Quartz properties
        schedulerFactoryBean.setQuartzProperties(quartzProperties);
    }

    private void resolveAndRegisterProviders() {
        // Resolve CarbonForecastApi and register in SpringBeanRegistry if a Spring bean is found
        ApiProviderResolver.ProviderResolutionResult<CarbonForecastApi> forecastResult =
            providerResolver.resolveForecastProvider(CarbonForecastApi.class);
        if (forecastResult.isSpringBeanResolved()) {
            SpringBeanRegistry.registerBean(CarbonForecastApi.class, forecastResult.getBeanInstance());
        }

        // Resolve PersistenceApi and register in SpringBeanRegistry if a Spring bean is found
        ApiProviderResolver.ProviderResolutionResult<PersistenceApi> persistenceResult =
            providerResolver.resolvePersistenceProvider(PersistenceApi.class);
        if (persistenceResult.isSpringBeanResolved()) {
            SpringBeanRegistry.registerBean(PersistenceApi.class, persistenceResult.getBeanInstance());
        }
    }
}
