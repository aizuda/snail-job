package com.aizuda.easy.retry.common.core.context;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * @author: www.byteblogs.com
 * @date : 2022-02-16 18:03
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@Slf4j
public class SpringContext implements ApplicationContextAware {

    public static ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        SpringContext.applicationContext = applicationContext;
    }

    public static <T> T getBeanByType(Class<T> clazz) {
        return applicationContext.getBean(clazz);
    }


    public static synchronized <T> T getBean(String name) {
        try {
            return (T) applicationContext.getBean(name);
        } catch (BeansException var2) {
            log.error(" BeanName:{} not exist，Exception => {}", name, var2.getMessage());
            return null;
        }
    }

    public static synchronized <T> T getBean(String name, Class<T> requiredType) {
        try {
            return applicationContext.getBean(name, requiredType);
        } catch (BeansException | NullPointerException var3) {
            log.error(" BeanName:{} not exist，Exception => {}", name, var3.getMessage());
            return null;
        }
    }

}
