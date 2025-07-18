package com.esentri.quartz.carbonaware.testsupport;

import com.esentri.quartz.carbonaware.entity.EmissionData;

import java.time.LocalDateTime;

public class EmissionDataImpl implements EmissionData {

    public EmissionDataImpl(LocalDateTime timestamp, Double value) {
        this.timestamp = timestamp;
        this.value = value;
    }

    LocalDateTime timestamp;
    Double value;

    @Override
    public LocalDateTime timestamp() {
        return timestamp;
    }

    @Override
    public Double value() {
        return value;
    }
}