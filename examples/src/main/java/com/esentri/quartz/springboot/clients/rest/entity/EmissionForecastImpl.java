package com.esentri.quartz.springboot.clients.rest.entity;

import com.esentri.quartz.carbonaware.entity.EmissionData;
import com.esentri.quartz.carbonaware.entity.EmissionForecast;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.util.ArrayList;
import java.util.List;

@Setter
@NoArgsConstructor
public class EmissionForecastImpl implements EmissionForecast {

    @Serial
    private static final long serialVersionUID = 1L;

    private String location;
    private Integer windowSize;
    private List<EmissionDataImpl> optimalDataPoints;

    @Override
    public String location() {
        return location;
    }

    @Override
    public Integer windowSize() {
        return windowSize;
    }

    @Override
    public List<EmissionData> optimalDataPoints() {
        return new ArrayList<>(optimalDataPoints);
    }
}
