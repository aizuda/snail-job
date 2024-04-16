package com.aizuda.snailjob.client.starter;

import com.aizuda.snailjob.client.core.annotation.Retryable;
import com.aizuda.snailjob.client.core.intercepter.SnailJobInterceptor;
import com.aizuda.snailjob.client.core.intercepter.SnailJobPointcutAdvisor;
import com.aizuda.snailjob.client.core.strategy.RetryStrategy;
import com.aizuda.snailjob.client.core.annotation.Retryable;
import com.aizuda.snailjob.client.core.intercepter.SnailJobInterceptor;
import com.aizuda.snailjob.client.core.intercepter.SnailJobPointcutAdvisor;
import com.aizuda.snailjob.client.core.strategy.RetryStrategy;
import org.aopalliance.intercept.MethodInterceptor;
import org.springframework.aop.Advisor;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.*;
import org.springframework.core.env.StandardEnvironment;

@Configuration
@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
@ConditionalOnClass(Retryable.class)
@ComponentScan({"com.aizuda.snailjob.client.core", "com.aizuda.snailjob.client.common"})
@ConditionalOnProperty(prefix = "snail-job", name = "enabled", havingValue = "true")
public class SnailJobClientRetryCoreAutoConfiguration {

    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    public Advisor snailJobPointcutAdvisor(MethodInterceptor snailJobInterceptor) {
        return new SnailJobPointcutAdvisor(snailJobInterceptor);
    }

    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    public MethodInterceptor snailJobInterceptor(StandardEnvironment standardEnvironment,
                                                 @Lazy RetryStrategy localRetryStrategies) {
        return new SnailJobInterceptor(standardEnvironment, localRetryStrategies);
    }

}
