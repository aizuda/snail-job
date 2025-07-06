package com.aizuda.snailjob.server.common.rpc.client.annotation;

import java.lang.annotation.*;

/**
 * 请求头信息
 *
 * @author: opensnail
 * @date : 2023-06-19 16:02
 * @since 2.0.0
 */
@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Header {

}
