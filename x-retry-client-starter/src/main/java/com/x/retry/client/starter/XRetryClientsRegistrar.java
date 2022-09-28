package com.x.retry.client.starter;

import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.env.Environment;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.type.AnnotationMetadata;

import java.util.Map;

/**
 * @author: www.byteblogs.com
 * @date : 2022-03-04 18:44
 */
public class XRetryClientsRegistrar implements ImportBeanDefinitionRegistrar, EnvironmentAware {

    private StandardEnvironment standardEnvironment;

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        Map<String, Object> attrs = importingClassMetadata.getAnnotationAttributes(EnableXRetry.class.getName());
        Map<String, Object> systemEnvironment = standardEnvironment.getSystemProperties();
        systemEnvironment.put("x-retry.group", (String) attrs.get("group"));
    }

    @Override
    public void setEnvironment(Environment environment) {
        StandardEnvironment standardEnvironment = (StandardEnvironment) environment;
        this.standardEnvironment = standardEnvironment;
        Map<String, Object> systemEnvironment = standardEnvironment.getSystemProperties();
        systemEnvironment.put("x-retry.enabled", true);
    }
}
