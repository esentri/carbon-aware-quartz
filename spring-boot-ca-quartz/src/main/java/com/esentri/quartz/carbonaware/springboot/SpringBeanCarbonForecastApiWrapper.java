package com.esentri.quartz.carbonaware.springboot;

import com.esentri.quartz.carbonaware.clients.rest.CarbonForecastApi;
import com.esentri.quartz.carbonaware.entity.EmissionForecast;

import java.time.LocalDateTime;
import java.util.List;

/**
 * SPI wrapper for {@link CarbonForecastApi} that delegates to a Spring-managed bean.
 * <p>
 * <b>god-mode enabled:</b> This class acts as a bridge between the Java SPI system
 * used by {@code CarbonAwarePlugin} and the Spring ApplicationContext. When loaded via SPI,
 * it retrieves the actual implementation from {@link SpringBeanRegistry} and delegates all calls to it.
 * <p>
 * This wrapper is registered in {@code META-INF/services} and will be discovered by the SPI
 * mechanism, allowing Spring beans to be used seamlessly by Quartz components.
 */
public class SpringBeanCarbonForecastApiWrapper implements CarbonForecastApi {

    private static final long serialVersionUID = 1L;

    /**
     * The Spring-managed delegate instance.
     */
    private final CarbonForecastApi delegate;

    /**
     * Constructs a new wrapper, retrieving the delegate from {@link SpringBeanRegistry}.
     */
    public SpringBeanCarbonForecastApiWrapper() {
        this.delegate = SpringBeanRegistry.getBean(CarbonForecastApi.class);
        if (this.delegate == null) {
            throw new IllegalStateException(
                "No CarbonForecastApi bean registered in SpringBeanRegistry. " +
                "Ensure @EnableCarbonAwareScheduling is present and a CarbonForecastApi bean is defined."
            );
        }
    }

    @Override
    public List<EmissionForecast> getEmissionForecastCurrent(
            List<String> location,
            LocalDateTime dataStartAt,
            LocalDateTime dataEndAt,
            Integer windowSize) {
        return delegate.getEmissionForecastCurrent(location, dataStartAt, dataEndAt, windowSize);
    }
}
