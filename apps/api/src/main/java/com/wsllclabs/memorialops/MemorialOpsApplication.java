package com.wsllclabs.memorialops;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@SpringBootApplication
@ConfigurationPropertiesScan
@EnableMethodSecurity
@EnableScheduling
public class MemorialOpsApplication {

    public static void main(String[] args) {
        SpringApplication.run(MemorialOpsApplication.class, args);
    }
}
