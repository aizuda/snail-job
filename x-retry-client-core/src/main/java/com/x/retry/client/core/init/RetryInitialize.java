package com.x.retry.client.core.init;

import com.x.retry.client.core.Scanner;
import com.x.retry.client.core.config.XRetryProperties;
import com.x.retry.client.core.register.RetryableRegistrar;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author: www.byteblogs.com
 * @date : 2022-03-03 16:50
 */
@Component
@Slf4j
public class RetryInitialize implements SmartInitializingSingleton, ApplicationContextAware {

    @Autowired
    private List<Scanner> scanners;

    @Autowired
    private RetryableRegistrar retryableRegistrar;

    @Autowired
    private XRetryProperties xRetryProperties;

    @Override
    public void afterSingletonsInstantiated() {

    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {

    }

}
