package com.x.retry.client.starter;

import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;

import java.util.Map;

/**
 * @author: www.byteblogs.com
 * @date : 2022-03-04 18:44
 */
public class XRetryClientsRegistrar implements ImportBeanDefinitionRegistrar {

    public static String GROUP = "";

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        Map<String, Object> attrs =  importingClassMetadata.getAnnotationAttributes(EnableXRetry.class.getName());
        XRetryClientsRegistrar.GROUP = (String) attrs.get("group");
    }
}
