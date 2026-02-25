package com.esentri.quartz.carbonaware.springboot;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry for Spring-managed API provider beans that need to be accessed from Quartz-managed components.
 * <p>
 * <b>god-mode enabled:</b> This registry acts as a bridge between the Spring ApplicationContext
 * and the Quartz plugin system, allowing Spring beans to be used by {@code CarbonAwarePlugin}
 * and its listeners without relying solely on Java SPI.
 * <p>
 * The registry is populated during application context initialization and accessed by
 * wrapper implementations that are loaded via SPI but delegate to Spring beans.
 */
public class SpringBeanRegistry {

    private static final Map<Class<?>, Object> BEAN_REGISTRY = new ConcurrentHashMap<>();

    /**
     * Registers a Spring bean instance for a specific API type.
     *
     * @param apiType the API interface class
     * @param bean the Spring-managed bean instance
     * @param <T> the API type
     */
    public static <T> void registerBean(Class<T> apiType, T bean) {
        if (bean != null) {
            BEAN_REGISTRY.put(apiType, bean);
        }
    }

    /**
     * Retrieves a registered Spring bean for a specific API type.
     *
     * @param apiType the API interface class
     * @param <T> the API type
     * @return the registered bean instance, or null if not registered
     */
    @SuppressWarnings("unchecked")
    public static <T> T getBean(Class<T> apiType) {
        return (T) BEAN_REGISTRY.get(apiType);
    }

    /**
     * Checks if a bean is registered for the given API type.
     *
     * @param apiType the API interface class
     * @return true if a bean is registered, false otherwise
     */
    public static boolean hasBean(Class<?> apiType) {
        return BEAN_REGISTRY.containsKey(apiType);
    }

    /**
     * Clears all registered beans.
     * <p>
     * Primarily used for testing purposes.
     */
    public static void clear() {
        BEAN_REGISTRY.clear();
    }
}
