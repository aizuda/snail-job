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
 * 2. 不得进行简单修改包装声称是自己的产品;
 * 3. 不得应用于危害国家安全、荣誉和利益的行为，不能以任何形式用于非法目的;
 *
 */
package com.aizuda.snailjob.client.core.annotation;

import com.aizuda.snailjob.client.core.IdempotentIdGenerate;
import com.aizuda.snailjob.client.core.callback.RetryCompleteCallback;
import com.aizuda.snailjob.client.core.callback.SimpleRetryCompleteCallback;
import com.aizuda.snailjob.client.core.generator.SimpleIdempotentIdGenerate;
import org.springframework.stereotype.Component;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

/**
 * 在使用手动生成重试任务时，通过ExecutorMethodRegister配置注册重试场景
 *
 * @author: opensnail
 * @date : 2023-05-10 09:39
 * @since 1.3.0
 */
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
@Component
public @interface ExecutorMethodRegister {

    /**
     * 场景值
     */
    String scene();

    /**
     * 幂等id生成器
     * 同一个组的同一个场景下只会存在一个相同的idempotentId并且状态为'重试中'的任务, 若存在相同的则上报服务后会被幂等处理
     * 比如:
     * 组: AGroup
     * 场景: BScene
     * 时刻1: 上报一个异常 idempotentId: A1 状态为重试中
     * 时刻2: 上报一个异常 idempotentId: A2 状态为重试中，可以上报成功，此时存在两个重试任务
     * 时刻3: 上报一个异常 idempotentId: A1 不会新增一个重试任务，会被幂等处理
     * 时刻4:  idempotentId: A1 重试完成, 状态为已完成
     * 时刻5: 上报一个异常 idempotentId: A1 状态为重试中, 新增一条重试任务
     * <p>
     * 默认的idempotentId生成器{@link SimpleIdempotentIdGenerate} 对所有参数进行MD5
     *
     * @return idempotentId
     */
    Class<? extends IdempotentIdGenerate> idempotentId() default SimpleIdempotentIdGenerate.class;

    /**
     * 服务端重试完成(重试成功、重试到达最大次数)回调客户端
     */
    Class<? extends RetryCompleteCallback> retryCompleteCallback() default SimpleRetryCompleteCallback.class;

    /**
     * 用于标识具有业务特点的值, 比如订单号、物流编号等，可以根据具体的业务场景生成，生成规则采用通用成熟的Spel表达式进行解析
     * <p>
     * see: <a href="https://docs.spring.io/spring-framework/docs/5.0.0.M5/spring-framework-reference/html/expressions.html">...</a>
     */
    String bizNo() default "";

    /**
     * 异步上报数据到服务端
     *
     * @return boolean
     */
    boolean async() default true;

    /**
     * 是否强制上报数据到服务端
     *
     * @return boolean
     */
    boolean forceReport() default false;

    /**
     * 同步(async:false)上报数据需要配置超时时间
     *
     * @return 超时时间
     */
    long timeout() default 60 * 1000;

    /**
     * 超时时间单位
     *
     * @return TimeUnit
     */
    TimeUnit unit() default TimeUnit.MILLISECONDS;

}
