package com.aizuda.snail.job.client.core.loader;

import cn.hutool.core.util.ServiceLoaderUtil;
import com.aizuda.snail.job.client.core.expression.ExpressionInvocationHandler;
import com.aizuda.snail.job.common.core.expression.ExpressionEngine;
import com.aizuda.snail.job.client.core.RetryArgSerializer;
import com.aizuda.snail.job.client.core.event.EasyRetryListener;
import com.aizuda.snail.job.client.core.event.SimpleEasyRetryListener;
import com.aizuda.snail.job.client.core.RetrySiteSnapshotContext;
import com.aizuda.snail.job.common.core.expression.ExpressionFactory;
import com.aizuda.snail.job.common.core.expression.strategy.SpELExpressionEngine;
import com.aizuda.snail.job.client.core.intercepter.ThreadLockRetrySiteSnapshotContext;
import com.aizuda.snail.job.client.core.serializer.JacksonSerializer;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Easy Retry Spi加载器
 *
 * @author: opensnail
 * @date : 2023-08-07 18:05
 * @since 2.2.0
 */
public class EasyRetrySpiLoader {

    private EasyRetrySpiLoader () {}

    /**
     * 加载参数序列化SPI类
     * 若配置多个则只加载第一个
     *
     * @return {@link JacksonSerializer} 默认序列化类为JacksonSerializer
     */
    public static RetryArgSerializer loadRetryArgSerializer() {
        return Optional.ofNullable(ServiceLoaderUtil.loadFirst(RetryArgSerializer.class)).orElse(new JacksonSerializer());
    }

    /**
     * 加载重试监听器SPI类
     * 执行顺序按照文件中定义的实现类的先后顺序
     *
     * @return {@link SimpleEasyRetryListener} 默认序列化类为SimpleEasyRetryListener
     */
    public static List<EasyRetryListener> loadEasyRetryListener() {
        List<EasyRetryListener> easyRetryListeners = ServiceLoaderUtil.loadList(EasyRetryListener.class);
        if (CollectionUtils.isEmpty(easyRetryListeners)) {
            return Collections.singletonList(new SimpleEasyRetryListener());
        }

        return easyRetryListeners;
    }

    /**
     * 加载重试现场记录上下文SPI类
     *
     * @return {@link RetrySiteSnapshotContext} 默认序列化类为ThreadLockRetrySiteSnapshotContext
     */
    public static <T> RetrySiteSnapshotContext<T> loadRetrySiteSnapshotContext() {
        return Optional.ofNullable(ServiceLoaderUtil.loadFirst(RetrySiteSnapshotContext.class)).orElse(new ThreadLockRetrySiteSnapshotContext<T>(new ThreadLocal<>()));
    }

    /**
     * 表达式引擎SPI类
     *
     * @return {@link SpELExpressionEngine} 默认序列化类为SpELExpressionEngine
     */
    public static ExpressionEngine loadExpressionEngine() {
        ExpressionEngine expressionEngine = Optional.ofNullable(ServiceLoaderUtil.loadFirst(ExpressionEngine.class)).orElse(new SpELExpressionEngine());
        return ExpressionFactory.getExpressionEngine(new ExpressionInvocationHandler(expressionEngine));
    }

}
