package com.esentri.quartz.springboot.configuration;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.io.Serial;
import java.io.Serializable;

@Configuration
public class RestTemplateConfiguration {
    @Bean
    public SerializableRestTemplate restTemplate() {
        return new RestTemplateBuilder()
                .configure(new SerializableRestTemplate());
    }

    public static class SerializableRestTemplate extends RestTemplate implements Serializable {

        @Serial
        private static final long serialVersionUID = 1L;

        public SerializableRestTemplate() {
            super();
        }
    }
}
