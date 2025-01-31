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

import java.lang.reflect.InvocationTargetException;
import java.util.Date;
import java.util.List;
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

        persistenceClient = tryToInstantiate(persistenceClientImplementationClass);
        restClient = tryToInstantiate(restClientImplementationClass);
        this.dryRun = dryRun;

    }

    private static <T> T tryToInstantiate(String implementationClassName) {
        try {
            Class<?> implementationClass = Class.forName(implementationClassName);
            return (T) implementationClass.getDeclaredConstructor().newInstance();

        } catch (NoSuchMethodException
                 | InstantiationException
                 | IllegalAccessException
                 | InvocationTargetException e) {
            throw new IllegalStateException("Cannot create instance of class for name: %s".formatted(implementationClassName), e);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Cannot find class for name: %s".formatted(implementationClassName), e);
        }
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
                            carbonAwareTrigger.getEmissionData().getValue(),
                            location,
                            dryRun));
        }
    }

    private static Double extractCurrentCarbonIntensity(List<EmissionForecast> currentEmissions, String location) {
        try {
            EmissionData emissionData = Functions.extractEmissionData(currentEmissions, location);
            return emissionData.getValue();
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