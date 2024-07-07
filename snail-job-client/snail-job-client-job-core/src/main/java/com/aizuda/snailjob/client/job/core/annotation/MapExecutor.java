package com.aizuda.snailjob.client.job.core.annotation;

import com.aizuda.snailjob.common.core.constant.SystemConstants;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author: opensnail
 * @date : 2024-06-26
 * @version : sj_1.1.0
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface MapExecutor {

    /**
     * 任务名称
     *
     * @return
     */
    String taskName() default SystemConstants.ROOT_MAP;

}
