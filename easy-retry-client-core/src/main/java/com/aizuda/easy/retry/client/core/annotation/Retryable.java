package com.aizuda.easy.retry.client.core.annotation;


import com.aizuda.easy.retry.client.core.IdempotentIdGenerate;
import com.aizuda.easy.retry.client.core.generator.SimpleIdempotentIdGenerate;
import com.aizuda.easy.retry.client.core.strategy.ExecutorAnnotationMethod;
import com.aizuda.easy.retry.client.core.strategy.ExecutorMethod;
import com.aizuda.easy.retry.client.core.callback.RetryCompleteCallback;
import com.aizuda.easy.retry.client.core.callback.SimpleRetryCompleteCallback;
import com.aizuda.easy.retry.client.core.retryer.RetryType;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

/**
 * 重试接入入口
 *
 * @author www.byteblogs.com
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Retryable {

    /**
     * 场景值
     */
    String scene();

    /**
     * 包含的异常
     */
    Class<? extends Throwable>[] include() default {};

    /**
     * 排除的异常
     */
    Class<? extends Throwable>[] exclude() default {};

    /**
     * 重试策略
     */
    RetryType retryStrategy() default RetryType.LOCAL_REMOTE;

    /**
     * 重试处理入口，默认为原方法
     *
     */
    Class<? extends ExecutorMethod> retryMethod() default ExecutorAnnotationMethod.class;

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
     *
     */
    Class<? extends RetryCompleteCallback> retryCompleteCallback() default SimpleRetryCompleteCallback.class;

    /**
     * 用于标识具有业务特点的值, 比如订单号、物流编号等，可以根据具体的业务场景生成，生成规则采用通用成熟的Spel表达式进行解析
     *
     * see: <a href="https://docs.spring.io/spring-framework/docs/5.0.0.M5/spring-framework-reference/html/expressions.html">...</a>
     */
    String bizNo() default "";

    /**
     * 本地重试次数 次数必须大于等于1
     */
    int localTimes() default 3;

    /**
     * 本地重试间隔时间(s)
     */
    int localInterval() default 2;

    /**
     * 本地重试完成后是否抛出异常
     * 如果不抛出异常，则调用需要重试方法的方法则感知不到异常信息
     *
     * 比如: A->B->C->D->E
     * D需要重试 若配置D不抛异常,则A->B->C无法感知，若有事物则无法回滚
     *
     * @return true-抛出 false-不抛出
     */
    boolean isThrowException() default true;

    /**
     * 异步上报数据到服务端
     *
     * @return boolean
     */
    boolean async() default true;

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

