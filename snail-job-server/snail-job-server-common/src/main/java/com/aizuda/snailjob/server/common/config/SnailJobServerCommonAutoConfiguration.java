package com.aizuda.snailjob.server.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

/**
 * web访问模块
 *
 * @author: opensnail
 * @date : 2023-09-19 09:21
 * @since : 2.4.0
 */
@Configuration
@ComponentScan("com.aizuda.snailjob.server.common.*")
public class SnailJobServerCommonAutoConfiguration {

    @Bean
    public TaskScheduler scheduledExecutorService() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(4);
        scheduler.setThreadNamePrefix("snail-job-scheduled-thread-");
        return scheduler;
    }
}
