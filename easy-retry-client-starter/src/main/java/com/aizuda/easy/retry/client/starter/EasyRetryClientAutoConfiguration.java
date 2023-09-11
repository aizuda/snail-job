package com.aizuda.easy.retry.client.starter;

import com.aizuda.easy.retry.client.core.intercepter.EasyRetryInterceptor;
import com.aizuda.easy.retry.client.core.intercepter.EasyRetryPointcutAdvisor;
import com.aizuda.easy.retry.client.core.strategy.RetryStrategy;
import org.aopalliance.intercept.MethodInterceptor;
import org.springframework.aop.Advisor;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Role;
import org.springframework.core.env.StandardEnvironment;

@Configuration
@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
@ComponentScan("com.aizuda.easy.retry.client.core")
@ConditionalOnProperty(prefix = "easy-retry", name = "enabled", havingValue = "true")
public class EasyRetryClientAutoConfiguration {

    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    public Advisor easyRetryPointcutAdvisor (MethodInterceptor easyRetryInterceptor) {
        return new EasyRetryPointcutAdvisor(easyRetryInterceptor);
    }

    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    public MethodInterceptor easyRetryInterceptor(StandardEnvironment standardEnvironment,
       @Lazy RetryStrategy localRetryStrategies) {
        return new EasyRetryInterceptor(standardEnvironment, localRetryStrategies);
    }

}
