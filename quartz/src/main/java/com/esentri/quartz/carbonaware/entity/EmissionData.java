package com.esentri.quartz.carbonaware.entity;

import java.time.LocalDateTime;

/**
 * Subset of Green Software Foundation SDK EmissionForecast
 *
 * @author jannisschalk
 * */
public interface EmissionData {

    LocalDateTime getTimestamp();

    Double getValue();
}
