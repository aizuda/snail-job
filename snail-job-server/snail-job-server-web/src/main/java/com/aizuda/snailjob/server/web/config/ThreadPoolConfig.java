package com.aizuda.snailjob.server.web.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@EnableAsync
@Configuration
public class ThreadPoolConfig {
    @Bean("logQueryExecutor")
    public ThreadPoolExecutor logQueryExecutor() {
        return new ThreadPoolExecutor(
                3,
                16,
                30,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(1000),
                new CustomizableThreadFactory("snail-job-log-query-"));
    }
}
