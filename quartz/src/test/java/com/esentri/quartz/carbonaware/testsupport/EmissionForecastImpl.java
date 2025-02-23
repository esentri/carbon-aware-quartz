package com.esentri.quartz.carbonaware.testsupport;

import com.esentri.quartz.carbonaware.entity.EmissionData;
import com.esentri.quartz.carbonaware.entity.EmissionForecast;

import java.util.List;

public class EmissionForecastImpl implements EmissionForecast {

    String location;
    Integer windowSize;
    List<EmissionData> emissionData;

    public EmissionForecastImpl(String location, Integer windowSize, List<EmissionData> emissionData) {
        this.location = location;
        this.windowSize = windowSize;
        this.emissionData = emissionData;
    }

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
        return emissionData;
    }
}
