package com.wsllclabs.memorialops.mocks.geocoding;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {
    "com.wsllclabs.memorialops.mocks.common",
    "com.wsllclabs.memorialops.mocks.geocoding"
})
public class GeocodingApplication {

    public static void main(String[] args) {
        SpringApplication.run(GeocodingApplication.class, args);
    }
}
