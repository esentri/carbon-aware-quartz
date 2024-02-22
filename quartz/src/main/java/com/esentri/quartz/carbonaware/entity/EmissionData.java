package com.esentri.quartz.carbonaware.entity;

import java.time.LocalDateTime;

/**
 * Subset of Green Software Foundation SDK EmissionForecast
 * */
public interface EmissionData {

    LocalDateTime getTimestamp();

    Double getValue();
}
