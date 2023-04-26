package com.example;

import com.aizuda.easy.retry.client.starter.EnableEasyRetry;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
//import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.core.Ordered;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableFeignClients(basePackages = "com.example.client")
@EnableEasyRetry(group = "example_group")
@EnableAspectJAutoProxy
//@EnableTransactionManagement(order = Ordered.LOWEST_PRECEDENCE - 10)
@EnableTransactionManagement
public class ExampleApplication {

    public static void main(String[] args) {
        SpringApplication.run(ExampleApplication.class, args);
    }

}
