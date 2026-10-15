package com.wsllclabs.memorialops.mocks.weather;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(
        scanBasePackages = {"com.wsllclabs.memorialops.mocks.common", "com.wsllclabs.memorialops.mocks.weather"})
public class WeatherAdvisoryApplication {

    public static void main(String[] args) {
        SpringApplication.run(WeatherAdvisoryApplication.class, args);
    }
}
