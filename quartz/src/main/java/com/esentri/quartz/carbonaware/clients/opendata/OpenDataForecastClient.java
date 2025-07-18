/*
 * Copyright (c) 2025 esentri AG
 *
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package com.esentri.quartz.carbonaware.clients.opendata;

import com.esentri.quartz.carbonaware.clients.opendata.model.CachedForecast;
import com.esentri.quartz.carbonaware.clients.opendata.model.Location;
import com.esentri.quartz.carbonaware.clients.rest.CarbonForecastApi;
import com.esentri.quartz.carbonaware.entity.EmissionData;
import com.esentri.quartz.carbonaware.entity.EmissionForecast;
import com.esentri.quartz.carbonaware.exceptions.NoForecastException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Default Client implementation of the interface {@link CarbonForecastApi} for retrieving carbon
 * emission forecasts using open data sources.
 * Implements the CarbonForecastApi interface to provide emission forecasts for specified locations
 * and time windows. Uses cached forecast data from {@link EnergyChartsForecastProvider}
 * to reduce rest-calls during execution. This reduces the carbon intensity of carbon-aware-scheduler.
 * Precondition to use this class is that the {@link EnergyChartsForecastProvider} is initialized
 *
 * @author jannisschalk
 */
public class OpenDataForecastClient implements CarbonForecastApi {

    private static final Logger LOGGER = LoggerFactory.getLogger(OpenDataForecastClient.class);
    public static final String MSG_NO_FORECAST_AVAILABLE = "No forecast available for location [%s]";

    /**
     * Retrieves emission forecasts for specified locations and time window.
     *
     * @param locations   List of location identifiers to get forecasts for
     * @param dataStartAt Start date/time for the forecast window
     * @param dataEndAt   End date/time for the forecast window
     * @param windowSize  Size of the time window in minutes
     * @return List of emission forecasts for each requested location
     * @throws NoForecastException if no forecast data is available for a location or if date boundaries are invalid
     */
    @Override
    public List<EmissionForecast> getEmissionForecastCurrent(
            List<String> locations,
            LocalDateTime dataStartAt,
            LocalDateTime dataEndAt,
            Integer windowSize) {
        var result = new ArrayList<EmissionForecast>();

        for (String locationCode : locations) {
            Location location = Location.fromCode(locationCode);

            CachedForecast forecast = EnergyChartsForecastProvider.getForecast(location);
            rejectInvalidForecastData(dataStartAt, location, forecast);

            final LocalDateTime finalDataEndAt = determineEndDateBasedOnMaximumForcastDateTime(dataEndAt, forecast);
            List<CachedForecast.CachedEmissionData> emissionDataRage = trimDataRangeToGivenTimeWindow(
                    dataStartAt,
                    finalDataEndAt,
                    forecast);

            EmissionDataImpl optimalEmissionData = findAbsoluteOrAverageMinimalCarbonIntensityWindow(
                    windowSize,
                    forecast.emissionData().get(0).duration(), //all durations are equal
                    emissionDataRage)

                    .orElseThrow(() -> new NoForecastException(MSG_NO_FORECAST_AVAILABLE.formatted(location.getDisplayName())));


            EmissionForecast emissionForecast = buildEmissionForcastObject(dataStartAt, windowSize, location, optimalEmissionData);

            result.add(emissionForecast);
        }
        return result;
    }

    private static void rejectInvalidForecastData(LocalDateTime dataStartAt,
                                                  Location location,
                                                  CachedForecast forecast) {
        if (forecast == null
                || forecast.emissionData() == null
                || forecast.emissionData().isEmpty()) {
            throw new NoForecastException(MSG_NO_FORECAST_AVAILABLE.formatted(location.getDisplayName()));
        }
        //check start date boundaries
        if (forecast.maximumForecastTimestamp().isBefore(dataStartAt)) {
            throw new NoForecastException("Start date %s is after forecasted maximum date %s"
                    .formatted(dataStartAt, forecast.maximumForecastTimestamp()));
        }
    }

    private static LocalDateTime determineEndDateBasedOnMaximumForcastDateTime(LocalDateTime dataEndAt,
                                                                               CachedForecast forecast) {
        if (dataEndAt.isAfter(forecast.maximumForecastTimestamp())) {
            LOGGER.warn(
                    "End date {} is after forecasted maximum date {}. Forecasted maximum date will be set as end date!",
                    dataEndAt,
                    forecast.maximumForecastTimestamp());

            return forecast.maximumForecastTimestamp();
        } else {
            return dataEndAt;
        }
    }

    private static List<CachedForecast.CachedEmissionData> trimDataRangeToGivenTimeWindow(LocalDateTime dataStartAt,
                                                                                          LocalDateTime finalDataEndAt,
                                                                                          CachedForecast forecast
    ) {
        return forecast.emissionData().stream()
                .filter(data -> data.timestamp().isAfter(dataStartAt.minusMinutes(data.duration()))) // start in the current active time window
                .filter(data -> data.timestamp().isBefore(finalDataEndAt))
                .collect(Collectors.toList());
    }

    private static Optional<EmissionDataImpl> findAbsoluteOrAverageMinimalCarbonIntensityWindow(
            Integer windowSize,
            double averageDuration,
            List<CachedForecast.CachedEmissionData> emissionDataRage) {

        return averageDuration >= windowSize
                ? findAbsoluteMinimumCarbonIntensity(emissionDataRage)
                : computeAverageMinimumCarbonIntensityOverMultipleDatapoints(emissionDataRage, windowSize, averageDuration);

    }

    private static EmissionForecast buildEmissionForcastObject(
            LocalDateTime dataStartAt,
            Integer windowSize,
            Location location,
            EmissionData optimalEmissionData) {
        // If the current time window is optimal. Execute immediately. Else, use forecasted timestamp
        if (optimalEmissionData.timestamp().isBefore(dataStartAt)) {
            return new EmissionForecastImpl(
                    location.getCode(),
                    windowSize,
                    List.of(new EmissionDataImpl(LocalDateTime.now().plusMinutes(1), optimalEmissionData.value()))
            );
        } else {
            return new EmissionForecastImpl(
                    location.getCode(),
                    windowSize,
                    List.of(optimalEmissionData));
        }
    }

    /**
     * Finds the data point with the absolute minimum carbon intensity within the given range.
     *
     * @param emissionDataRange List of emission data points to search through
     * @return Optional containing the data point with minimum carbon intensity, or empty if no data available
     */
    private static Optional<EmissionDataImpl> findAbsoluteMinimumCarbonIntensity(
            List<CachedForecast.CachedEmissionData> emissionDataRange
    ) {
        Optional<EmissionDataImpl> optimalEmissionData;
        optimalEmissionData = emissionDataRange.stream()
                .min(Comparator.comparing(CachedForecast.CachedEmissionData::value))
                .map(data -> new EmissionDataImpl(data.timestamp(), data.value()));
        return optimalEmissionData;
    }

    /**
     * Computes the optimal starting point for execution based on average carbon intensity over multiple data points.
     * Uses a sliding window approach to find the window with minimum average carbon intensity.
     *
     * @param data            List of emission data points to analyze
     * @param windowSize      Size of the time window in minutes
     * @param averageDuration Average duration between data points in minutes
     * @return Optional containing the optimal start point with averaged carbon intensity, or empty if insufficient data
     */
    private static Optional<EmissionDataImpl> computeAverageMinimumCarbonIntensityOverMultipleDatapoints(
            List<CachedForecast.CachedEmissionData> data,
            long windowSize,
            double averageDuration) {
        double value = windowSize / averageDuration;
        int amountOfDataPoints = (int) Math.floor(value) + (value > Math.floor(value) ? 1 : 0);

        // Check if enough data points are available
        if (data.size() < amountOfDataPoints) {
            return Optional.empty();
        }

        // Calculate the sum for the first window
        double currentSum = 0;
        for (int j = 0; j < amountOfDataPoints; j++) {
            currentSum += data.get(j).value();
        }

        // The first window sum is the initial minimum
        double minimalSum = currentSum;
        CachedForecast.CachedEmissionData optimalExecutionPoint = data.get(0);

        // Trim the possible rage, so the job with the duration of the "windowSize" can be executed before the end date
        int newSize = data.size() - amountOfDataPoints;
        if (newSize >= 0) {
            data = new ArrayList<>(data.subList(0, newSize));
        }


        // Sliding Window Algorithm for the next Data-Points
        for (int i = 1; i <= data.size() - amountOfDataPoints; i++) {
            // Subtract the value that leaves the window and add the new value
            currentSum = currentSum - data.get(i - 1).value() + data.get(i + amountOfDataPoints - 1).value();

            if (currentSum < minimalSum) {
                minimalSum = currentSum;
                optimalExecutionPoint = data.get(i);
            }
        }
        return optimalExecutionPoint == null
                ? Optional.empty()
                : Optional.of(new EmissionDataImpl(
                        optimalExecutionPoint.timestamp(),
                        minimalSum / amountOfDataPoints
                )
        );

    }

    private record EmissionForecastImpl(String location,
                                        Integer windowSize,
                                        List<EmissionData> optimalDataPoints) implements EmissionForecast {
    }

    private record EmissionDataImpl(LocalDateTime timestamp,
                                    Double value) implements EmissionData {
    }
}
