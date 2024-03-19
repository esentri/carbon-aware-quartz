package com.esentri.quartz.carbonaware.testsupport;

import com.esentri.quartz.carbonaware.clients.rest.CarbonForecastApi;
import com.esentri.quartz.carbonaware.entity.EmissionForecast;

import java.time.LocalDateTime;
import java.util.List;

public class CarbonForecastClient implements CarbonForecastApi {

    public double carbonIntensity = 63.7;
    public LocalDateTime optimalTimestamp = LocalDateTime.of(2024, 3, 19, 0, 0);
    public int windowSize1 = 10;

    @Override
    public List<EmissionForecast> getEmissionForecastCurrent(
            List<String> location,
            LocalDateTime dataStartAt,
            LocalDateTime dataEndAt,
            Integer windowSize) {


        return List.of(new EmissionForecastImpl(
                location.get(0),
                windowSize1,
                List.of(new EmissionDataImpl(
                        optimalTimestamp,
                        carbonIntensity))));
    }
}