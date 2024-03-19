package com.esentri.quartz.carbonaware.util;

import com.esentri.quartz.carbonaware.entity.EmissionData;
import com.esentri.quartz.carbonaware.entity.EmissionForecast;
import com.esentri.quartz.carbonaware.exceptions.ForecastUnavailableException;
import com.esentri.quartz.carbonaware.triggers.impl.CarbonAwareCronTriggerImpl;
import com.esentri.quartz.carbonaware.triggers.states.CarbonAwareExecutionState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.TimeZone;

public class Functions {

    public static EmissionData extractEmissionData(List<EmissionForecast> emissionForecasts, String location)
            throws ForecastUnavailableException {

        if(emissionForecasts == null || emissionForecasts.isEmpty()) {
           throw new ForecastUnavailableException("Emission forecast is not available");
        }

       return emissionForecasts.stream()
                .filter(forecast -> location.equals(forecast.getLocation()))
                .map(EmissionForecast::getOptimalDataPoints)
                .filter(Objects::nonNull)
                .flatMap(Collection::stream)
                .findFirst()
                .orElseThrow(() -> new ForecastUnavailableException("Emission forecast is not available"));
    }

    public static LocalDateTime convertDateToLocalDate(Date date, TimeZone timeZone) {
        if(date == null) {
            throw new IllegalArgumentException("Date must not be null");
        }

        return LocalDateTime.ofInstant(date.toInstant(), timeZone.toZoneId());
    }
}
