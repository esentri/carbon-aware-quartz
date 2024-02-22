package com.esentri.quartz.carbonaware.plugins;

import com.esentri.quartz.carbonaware.clients.CarbonForecastApi;
import com.esentri.quartz.carbonaware.triggers.CarbonAwareCronTrigger;
import org.quartz.JobExecutionContext;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.listeners.TriggerListenerSupport;
import org.quartz.spi.ClassLoadHelper;
import org.quartz.spi.SchedulerPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;

public class EmissionForecastPlugin extends TriggerListenerSupport implements SchedulerPlugin {

    private static final Logger LOGGER = LoggerFactory.getLogger(EmissionForecastPlugin.class);


    private CarbonForecastApi restClient;
    private String restClientImplementationClass;

    public EmissionForecastPlugin() {
        // intentionally left blank
    }

    @Override
    public void initialize(String name, Scheduler scheduler, ClassLoadHelper loadHelper) throws SchedulerException {
        LOGGER.info("Emission forecast plugin initialized...");
        LOGGER.info(restClientImplementationClass);

        scheduler.getListenerManager().addTriggerListener(this);
        restClient = tryToInstantiateRestClient(restClientImplementationClass);
    }

    private CarbonForecastApi tryToInstantiateRestClient(String restClientImplementationClass) {
        try {
            Class<?> implementationClass = Class.forName(restClientImplementationClass);
            return (CarbonForecastApi) implementationClass.getDeclaredConstructor().newInstance();

        } catch (NoSuchMethodException
                 | InstantiationException
                 | IllegalAccessException
                 | InvocationTargetException e) {
            throw new IllegalStateException(String.format("Cannot create instance of class for name: %s", restClientImplementationClass), e);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(String.format("Cannot find class for name: %s", restClientImplementationClass), e);
        }
    }

    @Override
    public void start() {

    }

    @Override
    public void shutdown() {

    }

    @Override
    public String getName() {
        return getClass().getName();
    }

    @Override
    public boolean vetoJobExecution(Trigger trigger, JobExecutionContext context) {
        if (trigger instanceof CarbonAwareCronTrigger) {
            CarbonAwareCronTrigger carbonAwareTrigger = (CarbonAwareCronTrigger) trigger;

            switch (carbonAwareTrigger.getTriggerState()) {
                case DETERMINED_BETTER_EXECUTION_TIME:
                    LOGGER.info("PLUGIN: Veto is true...");
                    return true;
                default:
                    LOGGER.info("PLUGIN: Veto is false...");
                    return false;
            }
        }
        return false;
    }

    // -------------- GETTER ----------------------------------------------------------------------------------//
    public CarbonForecastApi getRestClient() {
        return restClient;
    }

    public String getRestClientImplementationClass() {
        return restClientImplementationClass;
    }

    // -------------- SETTER ----------------------------------------------------------------------------------//
    public void setRestClient(CarbonForecastApi restClient) {
        this.restClient = restClient;
    }

    public void setRestClientImplementationClass(String restClientImplementationClass) {
        this.restClientImplementationClass = restClientImplementationClass;
    }


}
