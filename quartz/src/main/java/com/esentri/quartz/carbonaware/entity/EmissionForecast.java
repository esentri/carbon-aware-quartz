package com.esentri.quartz.carbonaware.entity;

import java.util.List;

/**
 * Subset of Green Software Foundation SDK EmissionForecast
 * */
public interface EmissionForecast {

    String getLocation();

    Integer getWindowSize();

    List<EmissionData> getOptimalDataPoints();
}
