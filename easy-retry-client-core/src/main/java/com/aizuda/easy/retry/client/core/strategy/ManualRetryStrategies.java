package com.aizuda.easy.retry.client.core.strategy;

import com.aizuda.easy.retry.client.core.RetryExecutor;
import com.aizuda.easy.retry.client.core.RetryExecutorParameter;
import com.aizuda.easy.retry.client.core.intercepter.RetrySiteSnapshot;
import com.aizuda.easy.retry.client.core.retryer.RetryType;
import com.aizuda.easy.retry.client.core.retryer.RetryerInfo;
import com.aizuda.easy.retry.client.core.retryer.RetryerResultContext;
import com.aizuda.easy.retry.common.core.enums.RetryResultStatusEnum;
import com.aizuda.easy.retry.common.core.log.LogUtils;
import com.github.rholder.retry.Attempt;
import com.github.rholder.retry.RetryListener;
import com.github.rholder.retry.StopStrategies;
import com.github.rholder.retry.StopStrategy;
import com.github.rholder.retry.WaitStrategies;
import com.github.rholder.retry.WaitStrategy;
import com.google.common.base.Predicate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * 手动执行重试
 *
 * @author: www.byteblogs.com
 * @date : 2023-05-15 18:19
 */
@Component
@Slf4j
public class ManualRetryStrategies extends AbstractRetryStrategies {

    @Override
    protected void setStage() {
        RetrySiteSnapshot.setStage(RetrySiteSnapshot.EnumStage.MANUAL_REPORT.getStage());
    }

    @Override
    protected Consumer<Object> doRetrySuccessConsumer(final RetryerResultContext context) {
        return o -> {
            LogUtils.debug(log, "ManualRetryStrategies doRetrySuccessConsumer ");
        };
    }

    @Override
    protected void error(final RetryerResultContext context) {
        context.setRetryResultStatusEnum(RetryResultStatusEnum.FAILURE);
    }

    @Override
    protected boolean preValidator(final RetryerInfo retryerInfo, final RetryerResultContext resultContext) {

        if (retryerInfo.isForceReport()) {
            return true;
        }

        if (RetrySiteSnapshot.isRunning()) {
            resultContext.setRetryResultStatusEnum(RetryResultStatusEnum.FAILURE);
            resultContext.setMessage("执行重试检验不通过 原因: 存在正在运行的重试任务");
            return false;
        }

        return true;
    }

    @Override
    protected void unexpectedError(final Exception e, final RetryerResultContext retryerResultContext) {
        retryerResultContext.setRetryResultStatusEnum(RetryResultStatusEnum.FAILURE);
    }

    @Override
    protected void success(final RetryerResultContext retryerResultContext) {
        retryerResultContext.setRetryResultStatusEnum(RetryResultStatusEnum.SUCCESS);
    }

    @Override
    protected Consumer<Throwable> doGetRetryErrorConsumer(final RetryerInfo retryerInfo, final Object[] params) {
        return throwable -> {
            LogUtils.debug(log, "ManualRetryStrategies doGetRetryErrorConsumer ");
        };
    }

    @Override
    protected Callable doGetCallable(final RetryExecutor<WaitStrategy, StopStrategy> retryExecutor, Object[] params) {
        RetryerInfo retryerInfo = retryExecutor.getRetryerInfo();
        return () -> doReport(retryerInfo, params);

    }

    @Override
    protected RetryExecutorParameter<WaitStrategy, StopStrategy> getRetryExecutorParameter(RetryerInfo retryerInfo) {
        return new RetryExecutorParameter<WaitStrategy, StopStrategy>() {

            @Override
            public WaitStrategy backOff() {
                return WaitStrategies.fixedWait(500, TimeUnit.MILLISECONDS);
            }

            @Override
            public StopStrategy stop() {
                return StopStrategies.stopAfterAttempt(5);
            }

            @Override
            public List<RetryListener> getRetryListeners() {
                return Collections.singletonList(new RetryListener() {
                    @Override
                    public <V> void onRetry(Attempt<V> attempt) {
                        if (attempt.hasResult()) {
                            LogUtils.info(log, "easy-retry 手动创建重试数据成功，第[{}]次调度", attempt.getAttemptNumber());
                        }

                        if (attempt.hasException()) {
                            LogUtils.error(log, "easy-retry 手动创建重试数据失败，第[{}]次调度 ", attempt.getAttemptNumber(),
                                attempt.getExceptionCause());
                        }

                    }
                });
            }
        };
    }

    @Override
    public boolean supports(final int stage, final RetryType retryType) {
        return RetrySiteSnapshot.EnumStage.MANUAL_REPORT.getStage() == stage;
    }
}
