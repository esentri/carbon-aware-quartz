package com.esentri.quartz.springboot.clients.jdbc;


import com.esentri.quartz.springboot.clients.jdbc.entity.CarbonStatistic;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CarbonStatisticsRepository extends CrudRepository<CarbonStatistic, String> {
}
