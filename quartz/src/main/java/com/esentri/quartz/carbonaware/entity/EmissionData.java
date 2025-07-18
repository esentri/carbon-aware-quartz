/*
 * Copyright (c) 2025 esentri AG
 *
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package com.esentri.quartz.carbonaware.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Subset of Green Software Foundation SDK EmissionForecast
 *
 * @author jannisschalk
 * */
public interface EmissionData extends Serializable {

    LocalDateTime timestamp();

    Double value();
}
