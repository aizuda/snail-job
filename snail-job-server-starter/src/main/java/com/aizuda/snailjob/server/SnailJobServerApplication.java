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
import com.aizuda.snailjob.common.core.util.SnailJobNetworkUtils;
import com.aizuda.snailjob.common.log.SnailJobLog;
import com.aizuda.snailjob.server.common.config.SystemProperties;
import com.aizuda.snailjob.server.common.rpc.server.grpc.GrpcServer;
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

    @Bean
    public Object configureSnailJobHost(SnailJobNetworkUtils networkUtils, SystemProperties systemProperties) {
        String host = systemProperties.getServerHost();
        if (StrUtil.isBlank(host)) {
            // 获取首选IP地址
            String serverIp = networkUtils.findPreferredHostAddress();
            systemProperties.setServerHost(serverIp);
            SnailJobLog.LOCAL.info("自动配置 Snail-Job 服务端IP为: {}" , serverIp);
        }
        return null;
    }

    @Bean
    public ApplicationRunner nettyStartupChecker(GrpcServer grpcServer,
                                                 ServletWebServerFactory serverFactory) {
        return args -> {
            // 判定Grpc
            boolean started =  grpcServer.isStarted();
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
}
