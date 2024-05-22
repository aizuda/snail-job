package com.aizuda.snailjob.server.common.rpc.client.annotation;

import com.aizuda.snailjob.server.common.rpc.client.RequestMethod;

import java.lang.annotation.*;

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
