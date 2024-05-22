package com.esentri.quartz.carbonaware.testsupport;

import com.esentri.quartz.carbonaware.clients.persistence.PersistenceApi;
import com.esentri.quartz.carbonaware.entity.CarbonStatisticDto;

import java.util.ArrayList;
import java.util.List;

public class PersistenceClient implements PersistenceApi {
    public List<CarbonStatisticDto> persistedObjects = new ArrayList<>();

    @Override
    public void persist(CarbonStatisticDto carbonStatisticDto) {
        persistedObjects.add(carbonStatisticDto);
    }
}