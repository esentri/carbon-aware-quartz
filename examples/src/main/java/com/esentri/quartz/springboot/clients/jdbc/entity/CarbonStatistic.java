package com.esentri.quartz.springboot.clients.jdbc.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "carbon_statistics")
public class CarbonStatistic {

    @Id
    @Column
    private String jobExecutionId;
    @Column
    private String jobName;
    @Column
    private String jobGroupName;
    @Column
    private Instant configuredTimestamp;
    @Column
    private Instant executionTimestamp;
    @Column
    private Integer jobDuration;
    @Column
    private Double actualCarbonIntensity;
    @Column
    private Double optimalCarbonIntensity;
    @Column
    private String location;
    @Column
    private Boolean dryRun;

}
