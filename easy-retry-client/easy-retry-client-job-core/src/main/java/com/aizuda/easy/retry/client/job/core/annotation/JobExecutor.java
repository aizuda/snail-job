package com.aizuda.easy.retry.client.job.core.annotation;

import java.lang.annotation.*;

/**
 * job执行者
 *
 * @author opensnail
 * @date 2023-09-26 23:19:01
 * @since 2.4.0
 */
@Target({ElementType.METHOD, ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface JobExecutor {

    /**
     * 执行器名称
     */
    String name();

    /**
     * 执行器方法
     */
    String method() default "jobExecute";
}
