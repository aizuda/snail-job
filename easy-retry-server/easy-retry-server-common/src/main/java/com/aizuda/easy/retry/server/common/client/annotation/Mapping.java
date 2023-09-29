package com.aizuda.easy.retry.server.common.client.annotation;

import com.aizuda.easy.retry.server.common.client.RequestMethod;
import com.aizuda.easy.retry.server.common.client.SimpleRetryListener;
import com.github.rholder.retry.RetryListener;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 接口定义
 *
 * @author: www.byteblogs.com
 * @date : 2023-05-11 22:32
 * @since 2.0.0
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Mapping {

    RequestMethod method() default RequestMethod.GET;

    String path() default "";

    boolean failover() default false;

    boolean failRetry() default false;

    int retryTimes() default 3;

    int retryInterval() default 1;

    Class<? extends RetryListener> retryListener() default SimpleRetryListener.class;



}
