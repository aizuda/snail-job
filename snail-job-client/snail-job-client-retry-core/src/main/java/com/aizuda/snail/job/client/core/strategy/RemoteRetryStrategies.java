package com.aizuda.snail.job.client.core.strategy;

import com.aizuda.snail.job.client.core.RetryExecutor;
import com.aizuda.snail.job.client.core.RetryExecutorParameter;
import com.aizuda.snail.job.client.core.intercepter.RetrySiteSnapshot;
import com.aizuda.snail.job.client.core.retryer.RetryType;
import com.aizuda.snail.job.client.core.retryer.RetryerResultContext;
import com.github.rholder.retry.*;
import com.aizuda.snail.job.client.core.retryer.RetryerInfo;
import com.aizuda.snail.job.common.core.enums.RetryResultStatusEnum;
import com.aizuda.snail.job.common.log.EasyRetryLog;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * 执行远程重试
 *
 * @author: opensnail
 * @date : 2022-03-03 14:38
 * @since 1.3.0
 */
@Component
@Slf4j
public class RemoteRetryStrategies extends AbstractRetryStrategies {

    @Override
    public boolean supports(int stage, RetryType retryType) {
        return RetrySiteSnapshot.EnumStage.REMOTE.getStage() == stage;
    }

    @Override
    protected void setStage() {
        RetrySiteSnapshot.setStage(RetrySiteSnapshot.EnumStage.REMOTE.getStage());
    }

    @Override
    protected Consumer<Object> doRetrySuccessConsumer(RetryerResultContext context) {
        return o -> {
            EasyRetryLog.LOCAL.debug("RemoteRetryStrategies doRetrySuccessConsumer ");
        };
    }

    @Override
    protected void error(RetryerResultContext context) {
        context.setRetryResultStatusEnum(RetryResultStatusEnum.FAILURE);
    }

    @Override
    protected boolean preValidator(RetryerInfo retryerInfo, RetryerResultContext resultContext) {
        if (RetrySiteSnapshot.isRunning()) {
            resultContext.setRetryResultStatusEnum(RetryResultStatusEnum.FAILURE);
            resultContext.setMessage("执行重试检验不通过 原因: 存在正在运行的重试任务");
            return false;
        }

        return true;
    }

    @Override
    protected void unexpectedError(Exception e, RetryerResultContext retryerResultContext) {
        retryerResultContext.setRetryResultStatusEnum(RetryResultStatusEnum.SUCCESS);
    }

    @Override
    protected void success(RetryerResultContext retryerResultContext) {
        retryerResultContext.setRetryResultStatusEnum(RetryResultStatusEnum.SUCCESS);
    }

    @Override
    protected Consumer<Throwable> doGetRetryErrorConsumer(RetryerInfo retryerInfo, Object[] params) {
        return throwable -> {
            EasyRetryLog.LOCAL.debug("RemoteRetryStrategies doGetRetryErrorConsumer ");
        };
    }

    @Override
    protected Callable doGetCallable(RetryExecutor<WaitStrategy, StopStrategy> retryExecutor, Object... params) {
        return () -> retryExecutor.execute(params);
    }

    @Override
    protected RetryExecutorParameter<WaitStrategy, StopStrategy> getRetryExecutorParameter(RetryerInfo retryerInfo) {
        return new RetryExecutorParameter<WaitStrategy, StopStrategy>() {

            @Override
            public WaitStrategy backOff() {
                return WaitStrategies.fixedWait(1, TimeUnit.SECONDS);
            }

            @Override
            public StopStrategy stop() {
                return StopStrategies.stopAfterAttempt(1);
            }

            @Override
            public List<RetryListener> getRetryListeners() {
                return Collections.singletonList(new RetryListener() {
                    @Override
                    public <V> void onRetry(Attempt<V> attempt) {
                        Integer attemptNumber = RetrySiteSnapshot.getAttemptNumber();
                        if (attempt.hasResult()) {
                           EasyRetryLog.LOCAL.info("snail-job 远程重试成功，第[{}]次调度", attemptNumber);
                        }

                        if (attempt.hasException()) {
                            EasyRetryLog.LOCAL.error("snail-job 远程重试失败，第[{}]次调度 ", attemptNumber, attempt.getExceptionCause());
                        }

                    }
                });
            }
        };
    }

}
