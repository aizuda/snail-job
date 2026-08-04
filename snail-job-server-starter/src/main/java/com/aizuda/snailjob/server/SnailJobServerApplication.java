/*
 * Copyright (c) 2024 .
 *
 * SnailJob - 灵活，可靠和快速的分布式任务重试和分布式任务调度平台
 * > ✅️ 可重放，可管控、为提高分布式业务系统一致性的分布式任务重试平台
 * > ✅️ 支持秒级、可中断、可编排的高性能分布式任务调度平台
 *
 * Aizuda/SnailJob 采用APACHE LICENSE 2.0开源协议，您在使用过程中，需要注意以下几点:
 *
 *
 * 1. 不得修改产品相关代码的源码头注释和出处;
 * 2. 不得应用于危害国家安全、荣誉和利益的行为，不能以任何形式用于非法目的;
 *
 */
package com.aizuda.snailjob.server;

import cn.hutool.core.util.StrUtil;
import com.aizuda.snailjob.server.common.rpc.server.grpc.GrpcServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.server.servlet.ServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.client.RestTemplate;

import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

@SpringBootApplication(scanBasePackages = {"com.aizuda.snailjob.server.starter.*"})
@EnableTransactionManagement(proxyTargetClass = true)
@Slf4j
public class SnailJobServerApplication {

    public static void main(String[] args) {
        // 初始化默认时区
        initDefaultTimeZone();
        // 启动SnailJob服务
        SpringApplication.run(SnailJobServerApplication.class, args);
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public ApplicationRunner nettyStartupChecker(GrpcServer grpcServer,
                                                 ServletWebServerFactory serverFactory) {
        return args -> {
            // 判定Grpc
            boolean started = grpcServer.isStarted();
            // 最长自旋10秒，保证 grpcHttpServer启动完成
            int waitCount = 0;
            while (!started && waitCount < 100) {
                log.info("--------> snail-job server is staring....");
                TimeUnit.MILLISECONDS.sleep(100);
                waitCount++;
                started = grpcServer.isStarted();
            }

            if (!started) {
                log.error("--------> snail-job server startup failure.");
                // Netty启动失败，停止Web服务和Spring Boot应用程序
                serverFactory.getWebServer().stop();
                SpringApplication.exit(SpringApplication.run(SnailJobServerApplication.class));
            }
        };
    }

    /**
     * 从环境变量读取时区配置，如果没有则使用系统默认时区
     * fixed 避免固定上海时区导致部署国际化服务的机房时，时区如何设置调整都为上海时区时间进行触发的问题
     */
    private static void initDefaultTimeZone() {
        String timezone = System.getenv("TZ");

        if (StrUtil.isNotBlank(timezone)) {
            try {
                TimeZone.setDefault(TimeZone.getTimeZone(timezone.trim()));
                log.info("--------> System timezone set to: {} from environment variable TZ", timezone);
            } catch (Exception e) {
                log.warn("--------> Invalid timezone '{}' from environment variable TZ, " +
                                "using system default timezone. Error: {}",
                        timezone, e.getMessage());
            }
            return;
        }

        log.info("--------> Environment variable TZ not set, using system default timezone: {}",
                TimeZone.getDefault().getID());
    }
}
