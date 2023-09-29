package com.aizuda.easy.retry.client.starter;

import com.aizuda.easy.retry.client.core.annotation.Retryable;
import com.aizuda.easy.retry.client.core.intercepter.EasyRetryInterceptor;
import com.aizuda.easy.retry.client.core.intercepter.EasyRetryPointcutAdvisor;
import com.aizuda.easy.retry.client.core.strategy.RetryStrategy;
import com.aizuda.easy.retry.client.job.core.annotation.JobExecutor;
import org.aopalliance.intercept.MethodInterceptor;
import org.springframework.aop.Advisor;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.*;
import org.springframework.core.env.StandardEnvironment;

@Configuration
@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
@ComponentScan({"com.aizuda.easy.retry.client.job.core", "com.aizuda.easy.retry.client.common"})
@ConditionalOnClass(JobExecutor.class)
@ConditionalOnProperty(prefix = "easy-retry", name = "enabled", havingValue = "true")
public class EasyRetryJobClientAutoConfiguration {

}
