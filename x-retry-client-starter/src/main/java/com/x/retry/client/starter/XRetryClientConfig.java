package com.x.retry.client.starter;

import com.x.retry.client.core.config.XRetryProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan("com.x.retry.client.core")
public class XRetryClientConfig {

    @Autowired
    public void setRetryProperties(XRetryProperties retryProperties) {
        XRetryProperties.setGroup(XRetryClientsRegistrar.GROUP);
    }
}
