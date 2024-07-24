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
