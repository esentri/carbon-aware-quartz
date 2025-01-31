/*
 * Copyright (c) 2025 esentri AG
 *
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package com.esentri.quartz.carbonaware.clients.persistence;

import com.esentri.quartz.carbonaware.entity.CarbonStatisticDto;

/**
 * Interface for a Persistence-Client to store the Carbon Intensity values
 *
 * @author jannisschalk
 * */
public interface PersistenceApi {

    void persist(CarbonStatisticDto carbonStatisticDto);
}
