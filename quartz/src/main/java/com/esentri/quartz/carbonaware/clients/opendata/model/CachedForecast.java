/*
 * Copyright (c) 2025 esentri AG
 *
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package com.esentri.quartz.carbonaware.clients.opendata.model;

import com.esentri.quartz.carbonaware.entity.EmissionData;

import java.time.LocalDateTime;
import java.util.List;


/**
 * Record representing cached forecast data for carbon emissions.
 *
 * @param lastUpdated              The timestamp when the forecast was last updated
 * @param maximumForecastTimestamp The maximum timestamp for which forecast data is available
 * @param emissionData             List of emission data points containing the actual forecast values
 *
 * @author jannisschalk
 */
public record CachedForecast(LocalDateTime lastUpdated,
                             LocalDateTime maximumForecastTimestamp,
                             List<CachedEmissionData> emissionData) {

    /**
     * Record representing a single emission data point in the forecast.
     *
     * @param timestamp  The timestamp for this emission data point
     * @param value     The emission value for this data point
     * @param duration  The duration in minutes for which this emission value is valid
     */
    public record CachedEmissionData(
            LocalDateTime timestamp,
            Double value,
            Long duration) implements EmissionData {

    }
}
