package com.esentri.quartz.carbonaware.entity;

import java.io.Serializable;
import java.util.List;

/**
 * Subset of Green Software Foundation SDK EmissionForecast
 *
 * @author jannisschalk
 * */
public interface EmissionForecast extends Serializable {

    String getLocation();

    Integer getWindowSize();

    List<EmissionData> getOptimalDataPoints();
}
