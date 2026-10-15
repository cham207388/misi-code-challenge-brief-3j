package com.wsllclabs.memorialops.mocks.registry;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(
        scanBasePackages = {"com.wsllclabs.memorialops.mocks.common", "com.wsllclabs.memorialops.mocks.registry"})
public class ApplicantRegistryApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApplicantRegistryApplication.class, args);
    }
}
