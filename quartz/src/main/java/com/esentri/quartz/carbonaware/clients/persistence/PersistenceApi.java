package com.esentri.quartz.carbonaware.clients.persistence;

import com.esentri.quartz.carbonaware.entity.Persistable;

public interface PersistenceApi {

    void persist(Persistable persistable);
}
