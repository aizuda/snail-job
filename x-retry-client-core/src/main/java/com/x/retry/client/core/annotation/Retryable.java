package com.x.retry.client.core.annotation;


import com.x.retry.client.core.BizIdGenerate;
import com.x.retry.client.core.generator.SimpleBizIdGenerate;
import com.x.retry.client.core.retryer.RetryType;
import com.x.retry.client.core.strategy.RetryAnnotationMethod;
import com.x.retry.client.core.strategy.RetryMethod;

import java.lang.annotation.*;

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
     * @return
     */
    Class<? extends RetryMethod> retryMethod() default RetryAnnotationMethod.class;

    /**
     * 自定义业务id，默认为hash(param),传入成员列表，全部拼接取hash
     *
     * @return
     */
    Class<? extends BizIdGenerate> bizId() default SimpleBizIdGenerate.class;

    /**
     * bizNo spel表达式
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

}

