/*
 * Copyright (c) 2024 .
 *
 * SnailJob - 灵活，可靠和快速的分布式任务重试和分布式任务调度平台
 * > ✅️ 可重放，可管控、为提高分布式业务系统一致性的分布式任务重试平台
 * > ✅️ 支持秒级、可中断、可编排的高性能分布式任务调度平台
 *
 * Aizuda/SnailJob 采用APACHE LICENSE 2.0开源协议，您在使用过程中，需要注意以下几点:
 *
 * 1. Aizuda/SnailJob已申请知识产权保护,二次开发如用于开源竞品请先联系群主沟通，禁止任何变相的二开行为，未经审核视为侵权;
 * 2. 不得修改产品相关代码的源码头注释和出处;
 * 3. 不得进行简单修改包装声称是自己的产品;
 * 4. 不得应用于危害国家安全、荣誉和利益的行为，不能以任何形式用于非法目的;
 *
 */
package com.aizuda.snailjob.client.job.core.annotation;

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
