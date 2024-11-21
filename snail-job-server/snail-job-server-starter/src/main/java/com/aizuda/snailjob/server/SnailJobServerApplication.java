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

import com.aizuda.snailjob.server.common.rpc.server.NettyHttpServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.server.ServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

@SpringBootApplication(scanBasePackages = {"com.aizuda.snailjob.server.starter.*"})
@EnableTransactionManagement(proxyTargetClass = true)
@Slf4j
public class SnailJobServerApplication {

    public static void main(String[] args) {
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Shanghai"));
        SpringApplication.run(SnailJobServerApplication.class, args);
    }

//    @Bean
    public ApplicationRunner nettyStartupChecker(NettyHttpServer nettyHttpServer, ServletWebServerFactory serverFactory) {
        return args -> {
            // 最长自旋10秒，保证nettyHttpServer启动完成
            int waitCount = 0;
            while (!nettyHttpServer.isStarted() && waitCount < 100) {
                log.info("--------> snail-job netty server is staring....");
                TimeUnit.MILLISECONDS.sleep(100);
                waitCount++;
            }

            if (!nettyHttpServer.isStarted()) {
                log.error("--------> snail-job netty server startup failure.");
                // Netty启动失败，停止Web服务和Spring Boot应用程序
                serverFactory.getWebServer().stop();
                SpringApplication.exit(SpringApplication.run(SnailJobServerApplication.class));
            }
        };
    }
}
