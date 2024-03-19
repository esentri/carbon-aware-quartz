package com.esentri.quartz.carbonaware.testsupport;

import com.esentri.quartz.carbonaware.clients.persistence.PersistenceApi;
import com.esentri.quartz.carbonaware.entity.Persistable;

import java.util.ArrayList;
import java.util.List;

public class PersistenceClient implements PersistenceApi {
    public List<Persistable> persistedObjects = new ArrayList<>();

    @Override
    public void persist(Persistable persistable) {
        persistedObjects.add(persistable);
    }
}