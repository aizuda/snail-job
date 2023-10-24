package com.aizuda.easy.retry.client.starter;

import com.aizuda.easy.retry.client.job.core.annotation.JobExecutor;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Role;

@Configuration
@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
@ComponentScan({"com.aizuda.easy.retry.client.job.core.*", "com.aizuda.easy.retry.client.common.*"})
@ConditionalOnClass(JobExecutor.class)
@ConditionalOnProperty(prefix = "easy-retry", name = "enabled", havingValue = "true")
public class EasyRetryJobClientAutoConfiguration {

}
