package com.esentri.quartz.example3.persistence;

import com.esentri.quartz.carbonaware.clients.persistence.PersistenceApi;
import com.esentri.quartz.carbonaware.entity.Persistable;

import java.util.ArrayList;
import java.util.List;

public class TestPersistenceClient implements PersistenceApi {

    List<Persistable> inMemoryPersistence = new ArrayList<>();

    @Override
    public void persist(Persistable persistable) {
        inMemoryPersistence.add(persistable);
    }
}
