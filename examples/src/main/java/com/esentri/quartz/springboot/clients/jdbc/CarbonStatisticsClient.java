package com.esentri.quartz.springboot.clients.jdbc;


import com.esentri.quartz.carbonaware.clients.persistence.PersistenceApi;
import com.esentri.quartz.carbonaware.entity.CarbonStatisticDto;
import com.esentri.quartz.springboot.clients.jdbc.entity.CarbonStatistic;
import com.esentri.quartz.springboot.configuration.ApplicationContextProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@DependsOn("applicationContextProvider")
public class CarbonStatisticsClient implements PersistenceApi {

    private final CarbonStatisticsRepository carbonStatisticsRepository;

    public CarbonStatisticsClient() {
        ApplicationContext applicationContext = ApplicationContextProvider.getApplicationContext();
        this.carbonStatisticsRepository = applicationContext.getBean(CarbonStatisticsRepository.class);
    }

    @Override
    public void persist(CarbonStatisticDto dto) {
        log.info("Persisting carbon statistics: {}", dto);

        carbonStatisticsRepository.save(
                new CarbonStatistic(
                        dto.jobExecutionId(),
                        dto.jobName(),
                        dto.jobGroupName(),
                        dto.configuredTimestamp(),
                        dto.executionTimestamp(),
                        dto.jobDuration(),
                        dto.carbonIntensityForConfiguredTimestamp(),
                        dto.carbonIntensityForRescheduledTimestamp(),
                        dto.location(),
                        dto.dryRun()));
    }
}
