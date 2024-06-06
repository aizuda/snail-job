/*
 * Copyright (c) 2024 .
 *
 * SnailJob - 灵活，可靠和快速的分布式任务重试和分布式任务调度平台
 * > ✅️ 可重放，可管控、为提高分布式业务系统一致性的分布式任务重试平台
 * > ✅️ 支持秒级、可中断、可编排的高性能分布式任务调度平台
 *
 * Aizuda/SnailJob 采用APACHE LICENSE 2.0开源协议，您在使用过程中，需要注意以下几点:
 *
 *
 * 1. 不得修改产品相关代码的源码头注释和出处;
 * 2. 不得应用于危害国家安全、荣誉和利益的行为，不能以任何形式用于非法目的;
 *
 */
package com.aizuda.snailjob.client.starter;

import org.springframework.context.annotation.Import;
import org.springframework.core.Ordered;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.lang.annotation.*;

/**
 * 在启动类上添加SnailJobRetry注解开启Snail Job功能
 *
 * @author: opensnail
 * @date : 2021-12-31 18:45
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(SnailJobClientsRegistrar.class)
public @interface EnableSnailJob {

    /**
     * 请在服务端提前配置好组,并设置在这里
     * group的配置支持注解和配置文件两种形式
     * 配置顺序为注解 > yml
     * 即: 如果注解内不配置默认取环境变量中的group配置
     * 比如:
     * <p>
     * snail-job.group = snail_job_demo_group
     * </p>
     *
     * @return group
     */
    String group() default "";

    /**
     * 控制多个Aop的执行顺序,
     * 需要注意的是这里顺序要比事务的Aop要提前
     * <p>
     * see {@link  EnableTransactionManagement#order()}
     * 默认值: Ordered.HIGHEST_PRECEDENCE
     */
    int order() default Ordered.HIGHEST_PRECEDENCE;


}
