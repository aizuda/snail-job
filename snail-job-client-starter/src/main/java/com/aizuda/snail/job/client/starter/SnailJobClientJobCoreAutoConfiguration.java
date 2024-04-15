package com.aizuda.snail.job.client.starter;

import com.aizuda.snail.job.client.job.core.annotation.JobExecutor;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Role;

@Configuration
@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
@ComponentScan({"com.aizuda.snail.job.client.job.core.*", "com.aizuda.snail.job.client.common.*"})
@ConditionalOnClass(JobExecutor.class)
@ConditionalOnProperty(prefix = "snail-job", name = "enabled", havingValue = "true")
public class SnailJobClientJobCoreAutoConfiguration {

}
