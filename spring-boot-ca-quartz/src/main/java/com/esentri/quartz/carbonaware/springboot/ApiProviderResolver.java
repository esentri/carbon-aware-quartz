package com.esentri.quartz.carbonaware.springboot;

import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;

import java.util.Map;

/**
 * Resolves API provider implementations from the Spring ApplicationContext.
 * <p>
 * <b>god-mode enabled:</b> Implements smart bean detection with the following precedence:
 * <ol>
 *   <li>Property-specified bean name ({@code bean-name})</li>
 *   <li>{@code @Primary} annotated bean</li>
 *   <li>Single auto-detected bean by type</li>
 *   <li>Property-specified implementation class ({@code impl-class}) - returned as class name for SPI bridging</li>
 *   <li>{@code null} - fallback to SPI default behavior</li>
 * </ol>
 * <p>
 * Fails fast if multiple beans exist without clear selection criteria.
 *
 * @see CarbonAwareProperties
 */
public class ApiProviderResolver {

    private final ApplicationContext applicationContext;
    private final CarbonAwareProperties properties;

    /**
     * Constructs a new API provider resolver.
     *
     * @param applicationContext the Spring application context for bean lookup
     * @param properties the carbon-aware configuration properties
     */
    public ApiProviderResolver(ApplicationContext applicationContext, CarbonAwareProperties properties) {
        this.applicationContext = applicationContext;
        this.properties = properties;
    }

    /**
     * Resolves the CarbonForecastApi provider.
     * <p>
     * Returns either:
     * <ul>
     *   <li>A Spring bean instance (if found via bean-name, @Primary, or single auto-detect)</li>
     *   <li>{@code null} if no bean found and no impl-class specified (SPI fallback)</li>
     * </ul>
     * Also returns the implementation class name if specified via {@code impl-class} property.
     *
     * @param <T> the API type
     * @param apiType the API class to resolve
     * @return resolution result containing bean instance and/or class name
     * @throws IllegalStateException if multiple beans found without clear selection
     */
    public <T> ProviderResolutionResult<T> resolveForecastProvider(Class<T> apiType) {
        String beanName = properties.getForecast().getBeanName();
        String implClass = properties.getForecast().getImplClass();

        return resolveProvider(apiType, beanName, implClass, "CarbonForecastApi");
    }

    /**
     * Resolves the PersistenceApi provider.
     * <p>
     * Returns either:
     * <ul>
     *   <li>A Spring bean instance (if found via bean-name, @Primary, or single auto-detect)</li>
     *   <li>{@code null} if no bean found and no impl-class specified (SPI fallback)</li>
     * </ul>
     * Also returns the implementation class name if specified via {@code impl-class} property.
     *
     * @param <T> the API type
     * @param apiType the API class to resolve
     * @return resolution result containing bean instance and/or class name
     * @throws IllegalStateException if multiple beans found without clear selection
     */
    public <T> ProviderResolutionResult<T> resolvePersistenceProvider(Class<T> apiType) {
        String beanName = properties.getPersistence().getBeanName();
        String implClass = properties.getPersistence().getImplClass();

        return resolveProvider(apiType, beanName, implClass, "PersistenceApi");
    }

    private <T> ProviderResolutionResult<T> resolveProvider(
            Class<T> apiType,
            String beanName,
            String implClass,
            String apiName) {

        // 1. Property-specified bean name (highest priority for Spring beans)
        if (beanName != null && !beanName.isBlank()) {
            try {
                T bean = applicationContext.getBean(beanName, apiType);
                return new ProviderResolutionResult<>(bean, null, "bean-name: " + beanName);
            } catch (NoSuchBeanDefinitionException e) {
                throw new IllegalStateException(
                    String.format("No bean found with name '%s' for type %s. " +
                        "Verify the bean name in carbon.aware.scheduling.%s.bean-name property.",
                        beanName, apiType.getName(), apiName.toLowerCase().replace("api", "")),
                    e
                );
            }
        }

        // 2. Check for @Primary bean or single auto-detected bean
        Map<String, T> beans = applicationContext.getBeansOfType(apiType);

        if (beans.isEmpty()) {
            // No Spring beans found
            // 3. Property-specified impl-class (for SPI bridging)
            if (implClass != null && !implClass.isBlank()) {
                return new ProviderResolutionResult<>(null, implClass, "impl-class: " + implClass);
            }
            // 4. Fallback to SPI default
            return new ProviderResolutionResult<>(null, null, "SPI fallback");
        }

        if (beans.size() == 1) {
            // Single bean found - auto-detect
            Map.Entry<String, T> entry = beans.entrySet().iterator().next();
            return new ProviderResolutionResult<>(entry.getValue(), null,
                "auto-detected single bean: " + entry.getKey());
        }

        // Multiple beans found - check for @Primary
        try {
            T primaryBean = applicationContext.getBean(apiType);
            return new ProviderResolutionResult<>(primaryBean, null, "@Primary bean");
        } catch (NoSuchBeanDefinitionException e) {
            // No @Primary found, multiple beans exist - fail fast
            throw new IllegalStateException(
                String.format("Multiple beans of type %s found: %s. " +
                    "Please mark one with @Primary or specify carbon.aware.scheduling.%s.bean-name property.",
                    apiType.getName(), beans.keySet(), apiName.toLowerCase().replace("api", ""))
            );
        }
    }

    /**
     * Result of API provider resolution.
     *
     * @param <T> the API type
     */
    public static class ProviderResolutionResult<T> {
        private final T beanInstance;
        private final String implementationClass;
        private final String resolutionMethod;

        /**
         * Constructs a new resolution result.
         *
         * @param beanInstance the resolved Spring bean instance, or null
         * @param implementationClass the implementation class name, or null
         * @param resolutionMethod description of how the provider was resolved
         */
        public ProviderResolutionResult(T beanInstance, String implementationClass, String resolutionMethod) {
            this.beanInstance = beanInstance;
            this.implementationClass = implementationClass;
            this.resolutionMethod = resolutionMethod;
        }

        /**
         * Returns the resolved Spring bean instance, or null if resolved via impl-class or SPI fallback.
         *
         * @return the bean instance, or null
         */
        public T getBeanInstance() {
            return beanInstance;
        }

        /**
         * Returns the implementation class name if specified via impl-class property, or null otherwise.
         *
         * @return the implementation class name, or null
         */
        public String getImplementationClass() {
            return implementationClass;
        }

        /**
         * Returns a description of how the provider was resolved (for logging/debugging).
         *
         * @return the resolution method description
         */
        public String getResolutionMethod() {
            return resolutionMethod;
        }

        /**
         * Returns true if a Spring bean was resolved (not impl-class or SPI fallback).
         *
         * @return true if a Spring bean was resolved
         */
        public boolean isSpringBeanResolved() {
            return beanInstance != null;
        }
    }
}
