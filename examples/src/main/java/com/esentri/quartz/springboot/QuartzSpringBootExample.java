package com.esentri.quartz.springboot;

import com.esentri.quartz.carbonaware.springboot.EnableCarbonAwareScheduling;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
@EnableCarbonAwareScheduling
public class QuartzSpringBootExample {

    public static void main(String[] args) {
        SpringApplication.run(QuartzSpringBootExample.class, args);
    }
}
