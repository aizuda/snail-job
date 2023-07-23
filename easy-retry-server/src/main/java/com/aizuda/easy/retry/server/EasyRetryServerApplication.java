package com.aizuda.easy.retry.server;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.util.TimeZone;

@SpringBootApplication
@MapperScan("com.aizuda.easy.retry.server.persistence.mybatis.mapper")
@EnableTransactionManagement(proxyTargetClass = true)
public class EasyRetryServerApplication {

    @Bean
    public TaskScheduler scheduledExecutorService() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(2);
        scheduler.setThreadNamePrefix("easy-retry-scheduled-thread-");
        return scheduler;
    }

    public static void main(String[] args) {
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Shanghai"));
        SpringApplication.run(EasyRetryServerApplication.class, args);
    }

}
