package com.aizuda.easy.retry.client.starter;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan("com.aizuda.easy.retry.client.core")
@ConditionalOnProperty(prefix = "easy-retry", name = "enabled", havingValue = "true")
public class XRetryClientAutoConfiguration {

}
