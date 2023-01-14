package com.x.retry.server;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.util.TimeZone;

@SpringBootApplication
@MapperScan("com.x.retry.server.persistence.mybatis.mapper")
@EnableTransactionManagement(proxyTargetClass = true)
public class XRetryServerApplication {

    public static void main(String[] args) {
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Shanghai"));
        SpringApplication.run(XRetryServerApplication.class, args);
    }

}
