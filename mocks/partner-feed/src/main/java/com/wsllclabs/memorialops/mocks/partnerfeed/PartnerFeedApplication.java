package com.wsllclabs.memorialops.mocks.partnerfeed;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {
    "com.wsllclabs.memorialops.mocks.common",
    "com.wsllclabs.memorialops.mocks.partnerfeed"
})
public class PartnerFeedApplication {

    public static void main(String[] args) {
        SpringApplication.run(PartnerFeedApplication.class, args);
    }
}
