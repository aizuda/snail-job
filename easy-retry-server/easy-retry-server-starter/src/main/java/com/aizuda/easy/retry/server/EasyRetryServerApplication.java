package com.aizuda.easy.retry.server;

import com.aizuda.easy.retry.server.starter.server.NettyHttpServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.server.ServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

@SpringBootApplication(scanBasePackages = {"com.aizuda.easy.retry.server.starter.*"})
@EnableTransactionManagement(proxyTargetClass = true)
@Slf4j
public class EasyRetryServerApplication {

    public static void main(String[] args) {
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Shanghai"));
        SpringApplication.run(EasyRetryServerApplication.class, args);
    }

    @Bean
    public ApplicationRunner nettyStartupChecker(NettyHttpServer nettyHttpServer, ServletWebServerFactory serverFactory) {
        return args -> {
            // 最长自旋10秒，保证nettyHttpServer启动完成
            int waitCount = 0;
            while (!nettyHttpServer.isStarted() && waitCount < 100) {
                log.info("--------> easy-retry netty server is staring....");
                TimeUnit.MILLISECONDS.sleep(100);
                waitCount++;
            }

            if (!nettyHttpServer.isStarted()) {
                log.error("--------> easy-retry netty server startup failure.");
                // Netty启动失败，停止Web服务和Spring Boot应用程序
                serverFactory.getWebServer().stop();
                SpringApplication.exit(SpringApplication.run(EasyRetryServerApplication.class));
            }
        };
    }
}
