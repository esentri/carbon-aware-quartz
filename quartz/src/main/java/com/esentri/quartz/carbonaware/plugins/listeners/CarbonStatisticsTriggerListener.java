/*
 * Copyright (c) 2025 esentri AG
 *
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package com.esentri.quartz.carbonaware.plugins.listeners;

import com.esentri.quartz.carbonaware.clients.persistence.PersistenceApi;
import com.esentri.quartz.carbonaware.clients.rest.CarbonForecastApi;
import com.esentri.quartz.carbonaware.entity.CarbonStatisticDto;
import com.esentri.quartz.carbonaware.entity.EmissionData;
import com.esentri.quartz.carbonaware.entity.EmissionForecast;
import com.esentri.quartz.carbonaware.exceptions.ForecastUnavailableException;
import com.esentri.quartz.carbonaware.triggers.CarbonAwareCronTrigger;
import com.esentri.quartz.carbonaware.triggers.states.CarbonAwareExecutionState;
import com.esentri.quartz.carbonaware.util.Functions;
import org.quartz.JobExecutionContext;
import org.quartz.Trigger;
import org.quartz.listeners.TriggerListenerSupport;

import java.util.Date;
import java.util.List;
import java.util.ServiceLoader;
import java.util.TimeZone;


/**
 * TriggerListener to listen on triggerFired events to store all determined carbon intensity values with a provided
 * {@link PersistenceApi}. To determine current carbon forecast an instance of {@link CarbonForecastApi} is required.
 *
 * @author jannisschalk
 * */
public class CarbonStatisticsTriggerListener extends TriggerListenerSupport {

    private final PersistenceApi persistenceClient;
    private final CarbonForecastApi restClient;
    private final Boolean dryRun;

    public CarbonStatisticsTriggerListener(
            String persistenceClientImplementationClass,
            String restClientImplementationClass,
            Boolean dryRun) {

        this.persistenceClient = loadProvider(
                com.esentri.quartz.carbonaware.clients.persistence.PersistenceApi.class,
                persistenceClientImplementationClass);
        this.restClient = loadProvider(
                com.esentri.quartz.carbonaware.clients.rest.CarbonForecastApi.class,
                restClientImplementationClass);
        this.dryRun = dryRun;

    }

    private static <T> T loadProvider(Class<T> apiType, String nameOrFqcn) {
        // Discover implementations via Java SPI (ServiceLoader) only — no reflection instantiation.
        for (T impl : ServiceLoader.load(apiType, Thread.currentThread().getContextClassLoader())) {
            Class<?> c = impl.getClass();
            if (c.getName().equals(nameOrFqcn) || c.getSimpleName().equals(nameOrFqcn)) {
                return impl;
            }
        }
        // If not found by an exact / simple name, but only one provider exists, return it to keep the default behavior.
        T single = null;
        int count = 0;
        for (T impl : ServiceLoader.load(apiType, Thread.currentThread().getContextClassLoader())) {
            single = impl;
            count++;
            if (count > 1) break;
        }
        if (count == 1 && (nameOrFqcn == null || nameOrFqcn.isBlank())) {
            return single;
        }
        throw new IllegalStateException(
                "No SPI provider for %s matching '%s'. Ensure an implementation is registered under META-INF/services/%s"
                        .formatted(apiType.getName(), nameOrFqcn, apiType.getName()));
    }

    @Override
    public void triggerFired(Trigger trigger, JobExecutionContext context) {
        if (trigger instanceof CarbonAwareCronTrigger carbonAwareTrigger
                && carbonAwareTrigger.getTriggerState() == CarbonAwareExecutionState.DETERMINED_BETTER_EXECUTION_TIME) {

            TimeZone timeZone = carbonAwareTrigger.getTimeZone();
            String location = carbonAwareTrigger.getLocation();
            int jobDuration = carbonAwareTrigger.getJobDuration();
            Date configuredExecutionTime = carbonAwareTrigger.getConfiguredExecutionTime();

            List<EmissionForecast> currentEmisions = restClient.getEmissionForecastCurrent(
                    List.of(location),
                    Functions.convertDateToLocalDate(configuredExecutionTime, timeZone),
                    Functions.convertDateToLocalDate(configuredExecutionTime, timeZone)
                            .plusMinutes(jobDuration)
                            .plusMinutes(1),
                    jobDuration);

            Double currentCarbonIntensity = extractCurrentCarbonIntensity(currentEmisions, location);

            persistenceClient.persist(
                    new CarbonStatisticDto(
                            context.getFireInstanceId(),
                            context.getJobDetail().getKey().getName(),
                            context.getJobDetail().getKey().getGroup(),
                            configuredExecutionTime.toInstant(),
                            carbonAwareTrigger.getOptimalExecutionTime().toInstant(),
                            jobDuration,
                            currentCarbonIntensity,
                            carbonAwareTrigger.getEmissionData().value(),
                            location,
                            dryRun));
        }
    }

    private static Double extractCurrentCarbonIntensity(List<EmissionForecast> currentEmissions, String location) {
        try {
            EmissionData emissionData = Functions.extractEmissionData(currentEmissions, location);
            return emissionData.value();
        } catch (ForecastUnavailableException e) {
            return null;
        }

    }

    @Override
    public String getName() {
        return getClass().getName();
    }

    public PersistenceApi getPersistenceClient() {
        return persistenceClient;
    }

    public CarbonForecastApi getRestClient() {
        return restClient;
    }
}