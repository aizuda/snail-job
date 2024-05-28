package com.aizuda.snailjob.server.retry.task.support.retry;

import cn.hutool.core.collection.CollUtil;
import com.aizuda.snailjob.server.common.WaitStrategy;
import com.aizuda.snailjob.server.common.exception.SnailJobServerException;
import com.aizuda.snailjob.server.retry.task.support.FilterStrategy;
import com.aizuda.snailjob.server.retry.task.support.RetryContext;
import com.aizuda.snailjob.server.retry.task.support.StopStrategy;

import java.util.*;

/**
 * 重试构建
 *
 * @author: opensnail
 * @date : 2021-11-29 18:42
 */
public class RetryBuilder<V> {

    private List<StopStrategy> stopStrategies;
    private WaitStrategy waitStrategy;
    private List<FilterStrategy> filterStrategies;
    private RetryContext<V> retryContext;

    public static <V> RetryBuilder<V> newBuilder() {
        return new RetryBuilder<>();
    }

    public RetryBuilder<V> withWaitStrategy(WaitStrategy waitStrategy) {
        this.waitStrategy = waitStrategy;
        return this;
    }

    public RetryBuilder<V> withFilterStrategy(FilterStrategy filterStrategy) {
        if (CollUtil.isEmpty(filterStrategies)) {
            filterStrategies = new ArrayList<>();
        }

        filterStrategies.add(filterStrategy);
        return this;
    }

    public RetryBuilder<V> withStopStrategy(StopStrategy stopStrategy) {
        if (CollUtil.isEmpty(stopStrategies)) {
            stopStrategies = new ArrayList<>();
        }

        stopStrategies.add(stopStrategy);
        return this;
    }

    public RetryBuilder<V> withRetryContext(RetryContext<V> retryContext) {
        this.retryContext = retryContext;
        return this;
    }

    public RetryExecutor<V> build() {

        if (Objects.isNull(waitStrategy)) {
            throw new SnailJobServerException("waitStrategy 不能为null");
        }

        if (Objects.isNull(retryContext)) {
            throw new SnailJobServerException("retryContext 不能为null");
        }

        if (CollUtil.isEmpty(stopStrategies)) {
            stopStrategies = Collections.EMPTY_LIST;
        } else {
            stopStrategies.sort(Comparator.comparingInt(StopStrategy::order));
        }

        if (CollUtil.isEmpty(filterStrategies)) {
            filterStrategies = Collections.EMPTY_LIST;
        } else {
            filterStrategies.sort(Comparator.comparingInt(FilterStrategy::order));
        }

        retryContext.setWaitStrategy(waitStrategy);

        return new RetryExecutor<V>(stopStrategies, waitStrategy, filterStrategies, retryContext);
    }

}
