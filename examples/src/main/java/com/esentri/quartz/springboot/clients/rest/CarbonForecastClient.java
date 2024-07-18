package com.esentri.quartz.springboot.clients.rest;

import com.esentri.quartz.carbonaware.clients.rest.CarbonForecastApi;
import com.esentri.quartz.carbonaware.entity.EmissionForecast;
import com.esentri.quartz.springboot.clients.jdbc.CarbonStatisticsRepository;
import com.esentri.quartz.springboot.clients.rest.entity.EmissionForecastImpl;
import com.esentri.quartz.springboot.configuration.ApplicationContextProvider;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.DependsOn;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@DependsOn("applicationContextProvider")
@Component
public class CarbonForecastClient implements CarbonForecastApi {

    private static final String URL = "https://forecast.carbon-aware-computing.com/emissions/forecasts/current" ;
    private static final String API_KEY= "<your api key>";

    private final RestTemplate restTemplate;

    public CarbonForecastClient() {
        ApplicationContext applicationContext = ApplicationContextProvider.getApplicationContext();
        this.restTemplate = applicationContext.getBean(RestTemplate.class);
    }


    @Override
    public List<EmissionForecast> getEmissionForecastCurrent(
            List<String> list,
            LocalDateTime localDateTime,
            LocalDateTime localDateTime1,
            Integer integer) {

        HttpHeaders headers = new HttpHeaders();
        headers.set("x-api-key", API_KEY);
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        Map<String, String> params = new HashMap<>();
        params.put("location", list.get(0));
        params.put("dataStartAt", localDateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        params.put("dataEndAt", localDateTime1.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        params.put("windowSize", String.valueOf(integer));


        String urlTemplate = UriComponentsBuilder.fromHttpUrl(URL)
                .queryParam("location", "{location}")
                .queryParam("dataStartAt", "{dataStartAt}")
                .queryParam("dataEndAt", "{dataEndAt}")
                .queryParam("windowSize", "{windowSize}")
                .encode()
                .toUriString();

        ResponseEntity<EmissionForecastResponse> result = restTemplate.exchange(urlTemplate, HttpMethod.GET, requestEntity, EmissionForecastResponse.class, params);

        return new ArrayList<>(result.getBody());
    }

    private static class EmissionForecastResponse extends ArrayList<EmissionForecastImpl> {
        // type
    }
}
