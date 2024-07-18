package com.esentri.quartz.springboot.clients.rest.entity;

import com.esentri.quartz.carbonaware.entity.EmissionData;
import com.esentri.quartz.carbonaware.entity.EmissionForecast;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Setter
@NoArgsConstructor
public class EmissionForecastImpl implements EmissionForecast {

    private String location;
    private Integer windowSize;
    private List<EmissionDataImpl> optimalDataPoints;

    @Override
    public String getLocation() {
        return location;
    }

    @Override
    public Integer getWindowSize() {
        return windowSize;
    }

    @Override
    public List<EmissionData> getOptimalDataPoints() {
        return new ArrayList<>(optimalDataPoints);
    }
}
