package com.wsllclabs.memorialops.mocks.calendar;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {
    "com.wsllclabs.memorialops.mocks.common",
    "com.wsllclabs.memorialops.mocks.calendar"
})
public class RegionalCalendarApplication {

    public static void main(String[] args) {
        SpringApplication.run(RegionalCalendarApplication.class, args);
    }
}
