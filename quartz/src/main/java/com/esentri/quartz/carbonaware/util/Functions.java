/*
 * Copyright (c) 2025 esentri AG
 *
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package com.esentri.quartz.carbonaware.util;

import com.esentri.quartz.carbonaware.entity.EmissionData;
import com.esentri.quartz.carbonaware.entity.EmissionForecast;
import com.esentri.quartz.carbonaware.exceptions.ForecastUnavailableException;

import java.time.LocalDateTime;
import java.util.*;

public class Functions {

    private Functions() {
        // hide the default constructor
    }

    public static EmissionData extractEmissionData(List<EmissionForecast> emissionForecasts, String location)
            throws ForecastUnavailableException {

        if(emissionForecasts == null || emissionForecasts.isEmpty()) {
           throw new ForecastUnavailableException("Emission forecast is not available");
        }

       return emissionForecasts.stream()
                .filter(forecast -> location.equals(forecast.location()))
                .map(EmissionForecast::optimalDataPoints)
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
