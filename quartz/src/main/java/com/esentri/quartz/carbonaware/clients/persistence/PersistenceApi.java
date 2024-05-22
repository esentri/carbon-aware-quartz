package com.esentri.quartz.carbonaware.clients.persistence;

import com.esentri.quartz.carbonaware.entity.CarbonStatisticDto;

public interface PersistenceApi {

    void persist(CarbonStatisticDto carbonStatisticDto);
}
