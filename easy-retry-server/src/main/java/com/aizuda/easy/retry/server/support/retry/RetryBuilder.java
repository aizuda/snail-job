package com.aizuda.easy.retry.server.support.retry;

import com.aizuda.easy.retry.server.exception.XRetryServerException;
import com.aizuda.easy.retry.server.support.FilterStrategy;
import com.aizuda.easy.retry.server.support.RetryContext;
import com.aizuda.easy.retry.server.support.StopStrategy;
import com.aizuda.easy.retry.server.support.WaitStrategy;
import org.springframework.util.CollectionUtils;

import java.util.*;

/**
 * 重试构建这
 *
 * @author: www.byteblogs.com
 * @date : 2021-11-29 18:42
 */
public class RetryBuilder<V> {

    private StopStrategy stopStrategy;
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
        if (CollectionUtils.isEmpty(filterStrategies)) {
            filterStrategies = new ArrayList<>();
        }

        filterStrategies.add(filterStrategy);
        return this;
    }

    public RetryBuilder<V> withStopStrategy(StopStrategy stopStrategy) {
        this.stopStrategy = stopStrategy;
        return this;
    }

    public RetryBuilder<V> withRetryContext(RetryContext<V> retryContext) {
        this.retryContext = retryContext;
        return this;
    }

    public RetryExecutor<V> build() {

        if (Objects.isNull(waitStrategy)) {
            throw new XRetryServerException("waitStrategy 不能为null");
        }

        if (Objects.isNull(stopStrategy)) {
            throw new XRetryServerException("stopStrategy 不能为null");
        }

        if (Objects.isNull(retryContext)) {
            throw new XRetryServerException("retryContext 不能为null");
        }

        if (CollectionUtils.isEmpty(filterStrategies)) {
            filterStrategies = Collections.EMPTY_LIST;
        } else {
            filterStrategies.sort(Comparator.comparingInt(FilterStrategy::order));
        }

        retryContext.setWaitStrategy(waitStrategy);

        return new RetryExecutor<V>(stopStrategy, waitStrategy, filterStrategies, retryContext);
    }

}
