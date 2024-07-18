package com.esentri.quartz.springboot.clients.rest.entity;

import com.esentri.quartz.carbonaware.entity.EmissionData;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EmissionDataImpl implements EmissionData {

    private OffsetDateTime timestamp;
    private Double value;

    @Override
    public LocalDateTime getTimestamp() {
        return timestamp.toLocalDateTime();
    }

    @Override
    public Double getValue() {
        return value;
    }
}
