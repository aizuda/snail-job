package com.x.retry.client.starter;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan("com.x.retry.client.core")
@ConditionalOnProperty(prefix = "x-retry", name = "enabled", havingValue = "true")
public class XRetryClientAutoConfiguration {

}
