package com.wsllclabs.memorialops.config;

import org.springframework.boot.task.SimpleAsyncTaskExecutorCustomizer;
import org.springframework.boot.task.ThreadPoolTaskExecutorCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AsyncConfig {

    @Bean
    ThreadPoolTaskExecutorCustomizer mdcThreadPoolCustomizer(MdcTaskDecorator decorator) {
        return executor -> executor.setTaskDecorator(decorator);
    }

    @Bean
    SimpleAsyncTaskExecutorCustomizer mdcSimpleAsyncCustomizer(MdcTaskDecorator decorator) {
        return executor -> executor.setTaskDecorator(decorator);
    }
}
