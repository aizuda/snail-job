package com.aizuda.snailjob.client.starter;

import cn.hutool.core.util.StrUtil;
import com.aizuda.snailjob.client.common.config.SnailJobProperties;
import com.aizuda.snailjob.client.job.core.annotation.JobExecutor;
import com.aizuda.snailjob.common.core.util.SnailJobNetworkUtils;
import com.aizuda.snailjob.common.log.SnailJobLog;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Role;

@Configuration
@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
@ComponentScan({"com.aizuda.snailjob.client.job.core.*", "com.aizuda.snailjob.client.common.*"})
@ConditionalOnClass(JobExecutor.class)
@ConditionalOnProperty(prefix = "snail-job", name = "enabled", havingValue = "true")
public class SnailJobClientJobCoreAutoConfiguration {

    @Bean
    public Object configureSnailJobHost(SnailJobNetworkUtils networkUtils, SnailJobProperties snailJobProperties) {
        String host = snailJobProperties.getHost();
        if (StrUtil.isBlank(host)) {
            // 获取首选IP地址
            String clientIp = networkUtils.findPreferredHostAddress();
            snailJobProperties.setHost(clientIp);
            SnailJobLog.LOCAL.info("自动配置 Snail-Job 客户端IP为: {}" , clientIp);
        }
        return null; // 不需要实际的bean实例，只是触发配置
    }
}
