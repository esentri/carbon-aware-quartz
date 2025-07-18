/*
 * Copyright (c) 2025 esentri AG
 *
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package com.esentri.quartz.carbonaware.entity;

import java.io.Serializable;
import java.util.List;

/**
 * Subset of Green Software Foundation SDK EmissionForecast
 *
 * @author jannisschalk
 * */
public interface EmissionForecast extends Serializable {

    String location();

    Integer windowSize();

    List<EmissionData> optimalDataPoints();
}
