package com.aizuda.easy.retry.client.common.annotation;

import com.aizuda.easy.retry.client.common.netty.RequestMethod;

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
 * @since 1.3.0
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Mapping {

    /**
     * 请求类型
     */
    RequestMethod method() default RequestMethod.GET;

    /**
     * 请求路径
     */
    String path() default "";

}
