package com.wsllclabs.memorialops.mocks.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(
        scanBasePackages = {"com.wsllclabs.memorialops.mocks.common", "com.wsllclabs.memorialops.mocks.gateway"})
public class NotificationGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(NotificationGatewayApplication.class, args);
    }
}
