package com.esentri.quartz.carbonaware.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Subset of Green Software Foundation SDK EmissionForecast
 *
 * @author jannisschalk
 * */
public interface EmissionData extends Serializable {

    LocalDateTime getTimestamp();

    Double getValue();
}
