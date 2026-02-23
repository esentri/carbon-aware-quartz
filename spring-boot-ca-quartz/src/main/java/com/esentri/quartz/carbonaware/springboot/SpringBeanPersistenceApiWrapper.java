package com.esentri.quartz.carbonaware.springboot;

import com.esentri.quartz.carbonaware.clients.persistence.PersistenceApi;
import com.esentri.quartz.carbonaware.entity.CarbonStatisticDto;

/**
 * SPI wrapper for {@link PersistenceApi} that delegates to a Spring-managed bean.
 * <p>
 * <b>god-mode enabled:</b> This class acts as a bridge between the Java SPI system
 * used by {@code CarbonAwarePlugin} and the Spring ApplicationContext. When loaded via SPI,
 * it retrieves the actual implementation from {@link SpringBeanRegistry} and delegates all calls to it.
 * <p>
 * This wrapper is registered in {@code META-INF/services} and will be discovered by the SPI
 * mechanism, allowing Spring beans to be used seamlessly by Quartz components.
 */
public class SpringBeanPersistenceApiWrapper implements PersistenceApi {

    /**
     * The Spring-managed delegate instance.
     */
    private final PersistenceApi delegate;

    /**
     * Constructs a new wrapper, retrieving the delegate from {@link SpringBeanRegistry}.
     */
    public SpringBeanPersistenceApiWrapper() {
        this.delegate = SpringBeanRegistry.getBean(PersistenceApi.class);
        if (this.delegate == null) {
            throw new IllegalStateException(
                "No PersistenceApi bean registered in SpringBeanRegistry. " +
                "Ensure @EnableCarbonAwareScheduling is present and a PersistenceApi bean is defined."
            );
        }
    }

    @Override
    public void persist(CarbonStatisticDto carbonStatisticDto) {
        delegate.persist(carbonStatisticDto);
    }
}
