package com.aizuda.easy.retry.server.common.rpc.client.annotation;

import com.aizuda.easy.retry.server.common.rpc.client.RequestMethod;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 接口定义
 *
 * @author: opensnail
 * @date : 2023-05-11 22:32
 * @since 2.0.0
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Mapping {

    RequestMethod method() default RequestMethod.GET;

    String path() default "";

}
