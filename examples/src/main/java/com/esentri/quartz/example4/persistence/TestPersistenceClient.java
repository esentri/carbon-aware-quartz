package com.esentri.quartz.example4.persistence;

import com.esentri.quartz.carbonaware.clients.persistence.PersistenceApi;
import com.esentri.quartz.carbonaware.entity.CarbonStatisticDto;

import java.util.ArrayList;
import java.util.List;

public class TestPersistenceClient implements PersistenceApi {

    List<CarbonStatisticDto> inMemoryPersistence = new ArrayList<>();

    @Override
    public void persist(CarbonStatisticDto carbonStatisticDto) {
        inMemoryPersistence.add(carbonStatisticDto);
    }
}
