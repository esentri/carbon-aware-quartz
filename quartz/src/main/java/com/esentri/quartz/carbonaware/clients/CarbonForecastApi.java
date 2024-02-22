package com.esentri.quartz.carbonaware.clients;



import com.esentri.quartz.carbonaware.entity.EmissionForecast;

import java.time.LocalDateTime;
import java.util.List;


/**
 * Interface for a REST-Client to Get the best execution time with minimal grid carbon intensity.
 * <br>
 * This is a minimal subset of the
 * <a href="https://greensoftware.foundation/projects"><b>Green Software Foundation CarbonAware SDK</b></a>.
 *
 * @author jannisschalk
 * */
public interface CarbonForecastApi {

    /**
     * Get the best execution time with minimal grid carbon intensity.
     * A time intervall of the given duration within the earliest and latest execution
     * time with the most renewable energy in the power grid of the location.
     *
     *
     * @param location  list of named locations like (de,fr).
     * @param dataStartAt Start time boundary of forecasted data points.
     *                    Ignores current forecast data points before this time.
     *                    Defaults to the earliest time in the forecast data.
     * @param dataEndAt End time boundary of forecasted data points.
     *                  Ignores current forecast data points after this time.
     *                  Defaults to the latest time in the forecast data.
     * @param windowSize The estimated duration (in minutes) of the workload.
     * */
    List<EmissionForecast> getEmissionForecastCurrent(
            List<String> location,
            LocalDateTime dataStartAt,
            LocalDateTime dataEndAt,
            Integer windowSize
    );
}
