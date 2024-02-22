package com.esentri.quartz.example1.restclient;



import com.esentri.quartz.carbonaware.clients.CarbonForecastApi;
import com.esentri.quartz.carbonaware.entity.EmissionData;
import com.esentri.quartz.carbonaware.entity.EmissionForecast;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

/**
 *
 * Simple Mock implementation of the {@link CarbonForecastApi}
 * This example uses Seconds as ChronoUnit, but as defined by CarbonAware SDK the Window-Size is defined in Minutes.
 *
 * @author jannisschalk
 * */
public class TestClient implements CarbonForecastApi {


    public TestClient() {
        // intentionally left blank
    }

    @Override
    public List<EmissionForecast> getEmissionForecastCurrent(List<String> location, LocalDateTime dataStartAt, LocalDateTime dataEndAt, Integer windowSize) {
        return List.of(new EmissionForecast() {
            @Override
            public String getLocation() {
                return location.get(0);
            }

            @Override
            public Integer getWindowSize() {
                return windowSize;
            }

            @Override
            public List<EmissionData> getOptimalDataPoints() {
                return List.of(new EmissionData() {
                    @Override
                    public LocalDateTime getTimestamp() {
                        return LocalDateTime.now().plusSeconds(20);
                    }

                    @Override
                    public Double getValue() {
                        return 22.7;
                    }
                });
            }
        });
    }
}
