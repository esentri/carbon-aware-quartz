package com.esentri.quartz.springboot.clients.rest;

import com.esentri.quartz.carbonaware.clients.rest.CarbonForecastApi;
import com.esentri.quartz.carbonaware.entity.EmissionForecast;
import com.esentri.quartz.springboot.clients.rest.entity.EmissionForecastImpl;
import com.esentri.quartz.springboot.configuration.ApplicationContextProvider;
import com.esentri.quartz.springboot.configuration.RestTemplateConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.DependsOn;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.Serial;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@DependsOn("applicationContextProvider")
@Component
public class CarbonForecastClient implements CarbonForecastApi {

    @Serial
    private static final long serialVersionUID = 1L;

    private static final String URL = "https://forecast.carbon-aware-computing.com/emissions/forecasts/current" ;
    private static final String API_KEY= "<your api key>";

    private final RestTemplateConfiguration.SerializableRestTemplate restTemplate;

    public CarbonForecastClient() {
        ApplicationContext applicationContext = ApplicationContextProvider.getApplicationContext();
        this.restTemplate = applicationContext.getBean(RestTemplateConfiguration.SerializableRestTemplate.class);
    }

    @Override
    public List<EmissionForecast> getEmissionForecastCurrent(
            List<String> location,
            LocalDateTime startTime,
            LocalDateTime endTime,
            Integer jobDuration) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("x-api-key", API_KEY);
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        Map<String, String> params = new HashMap<>();
        params.put("location", location.get(0));
        params.put("dataStartAt", startTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        params.put("dataEndAt", endTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        params.put("windowSize", String.valueOf(jobDuration));

        String urlTemplate = UriComponentsBuilder.fromUriString(URL)
                .queryParam("location", "{location}")
                .queryParam("dataStartAt", "{dataStartAt}")
                .queryParam("dataEndAt", "{dataEndAt}")
                .queryParam("windowSize", "{windowSize}")
                .encode()
                .toUriString();
        ResponseEntity<EmissionForecastResponse> result = restTemplate.exchange(urlTemplate, HttpMethod.GET, requestEntity, EmissionForecastResponse.class, params);

        return new ArrayList<>(Objects.requireNonNull(result.getBody()));
    }

    private static class EmissionForecastResponse extends ArrayList<EmissionForecastImpl> {
        // type
    }
}
