package com.esentri.quartz.shared.clients.forecast;



import com.esentri.quartz.carbonaware.clients.rest.CarbonForecastApi;
import com.esentri.quartz.carbonaware.entity.EmissionData;
import com.esentri.quartz.carbonaware.entity.EmissionForecast;

import java.time.LocalDateTime;
import java.util.List;

/**
 *
 * Simple Mock implementation of the {@link CarbonForecastApi}
 * This example uses Seconds as ChronoUnit, but as defined by CarbonAware SDK the Window-Size is defined in Minutes.
 *
 * @author jannisschalk
 * */
public class TestForecastClient implements CarbonForecastApi {


    public TestForecastClient() {
        // intentionally left blank
    }

    @Override
    public List<EmissionForecast> getEmissionForecastCurrent(List<String> location, LocalDateTime dataStartAt, LocalDateTime dataEndAt, Integer windowSize) {
        return List.of(new EmissionForecast() {
            @Override
            public String location() {
                return location.get(0);
            }

            @Override
            public Integer windowSize() {
                return windowSize;
            }

            @Override
            public List<EmissionData> optimalDataPoints() {
                return List.of(new EmissionData() {
                    @Override
                    public LocalDateTime timestamp() {
                        return LocalDateTime.now().plusSeconds(20);
                    }

                    @Override
                    public Double value() {
                        return 22.7;
                    }
                });
            }
        });
    }
}
