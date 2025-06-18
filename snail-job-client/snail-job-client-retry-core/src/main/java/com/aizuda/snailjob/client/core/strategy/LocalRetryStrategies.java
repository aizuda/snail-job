package com.aizuda.snailjob.client.core.strategy;

import com.aizuda.snailjob.client.core.RetryExecutor;
import com.aizuda.snailjob.client.core.RetryExecutorParameter;
import com.aizuda.snailjob.client.core.exception.SnailRetryClientException;
import com.aizuda.snailjob.client.core.intercepter.RetrySiteSnapshot;
import com.aizuda.snailjob.client.core.retryer.RetryType;
import com.aizuda.snailjob.client.core.retryer.RetryerInfo;
import com.aizuda.snailjob.client.core.retryer.RetryerResultContext;
import com.aizuda.snailjob.common.core.enums.RetryResultStatusEnum;
import com.aizuda.snailjob.common.log.SnailJobLog;
import com.github.rholder.retry.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * 执行本地重试
 *
 * @author: opensnail
 * @date : 2022-03-03 14:38
 * @since 1.3.0
 */
@Component
@Slf4j
public class LocalRetryStrategies extends AbstractRetryStrategies {

    @Override
    public boolean supports(int stage, RetryType retryType) {
        return RetrySiteSnapshot.EnumStage.LOCAL.getStage() == stage;
    }

    @Override
    protected void setStage() {
        RetrySiteSnapshot.setStage(RetrySiteSnapshot.EnumStage.LOCAL.getStage());
    }

    @Override
    protected Consumer<Object> doRetrySuccessConsumer(RetryerResultContext context) {
        return o -> {

            RetryerInfo retryerInfo = context.getRetryerInfo();
            if (retryerInfo.getRetryType() == RetryType.ONLY_REMOTE) {
                // 若是远程重试模式，采用直接上报服务端
                // 这里标志结果为失败，是表示数据并未重试成功，等待服务端重试
                context.setRetryResultStatusEnum(RetryResultStatusEnum.FAILURE);
            } else {
                SnailJobLog.LOCAL.debug("doRetrySuccessConsumer retry successful");
            }
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
            resultContext.setMessage("Retry validation failed: reason: there is an ongoing retry task");
            return false;
        }

        if (RetrySiteSnapshot.isRetryForStatusCode()) {
            resultContext.setRetryResultStatusEnum(RetryResultStatusEnum.FAILURE);
            resultContext.setMessage("Retry validation failed: reason: downstream flag prohibits retry");
            return false;
        }

        return true;
    }

    @Override
    protected void unexpectedError(Exception e, RetryerResultContext retryerResultContext) {
        retryerResultContext.setRetryResultStatusEnum(RetryResultStatusEnum.FAILURE);
    }

    @Override
    protected void success(RetryerResultContext retryerResultContext) {
        retryerResultContext.setRetryResultStatusEnum(RetryResultStatusEnum.SUCCESS);
    }

    @Override
    public Consumer<Throwable> doGetRetryErrorConsumer(RetryerInfo retryerInfo, Object[] params) {
        return throwable -> {
            // 执行上报服务端
            log.info("Memory retry completed but the exception was not resolved scene:[{}]", retryerInfo.getScene());
            // 上报服务端异常
            if (RetryType.LOCAL_REMOTE.name().equals(retryerInfo.getRetryType().name())) {
                // 上报
                log.debug("Report scene:[{}]", retryerInfo.getScene());
                doReport(retryerInfo, params);
            }
        };
    }

    @Override
    public Callable doGetCallable(RetryExecutor<WaitStrategy, StopStrategy> retryExecutor, Object... params) {

        RetryerInfo retryerInfo = retryExecutor.getRetryerInfo();
        RetryType retryType = retryerInfo.getRetryType();
        switch (retryType) {
            // 如果是仅仅本地重试或本地_远程模式则先支持重试
            case ONLY_LOCAL:
            case LOCAL_REMOTE:

                // 标记进入方法的时间
                RetrySiteSnapshot.setEntryMethodTime(System.currentTimeMillis());

                return () -> retryExecutor.execute(params);
            case ONLY_REMOTE:
                // 仅仅是远程重试则直接上报
                log.debug("Report scene:[{}]", retryerInfo.getScene());
                doReport(retryerInfo, params);
                RetrySiteSnapshot.setStage(RetrySiteSnapshot.EnumStage.REMOTE.getStage());
                return () -> null;
            default:
                throw new SnailRetryClientException("Exception retry mode [{}]", retryType.name());
        }

    }

    @Override
    public RetryExecutorParameter<WaitStrategy, StopStrategy> getRetryExecutorParameter(RetryerInfo retryerInfo) {

        return new RetryExecutorParameter<WaitStrategy, StopStrategy>() {

            @Override
            public WaitStrategy backOff() {
                return WaitStrategies.fixedWait(retryerInfo.getLocalInterval(), TimeUnit.SECONDS);
            }

            @Override
            public StopStrategy stop() {
                return StopStrategies.stopAfterAttempt(retryerInfo.getLocalTimes());
            }

            @Override
            public List<RetryListener> getRetryListeners() {
                return Collections.singletonList(new RetryListener() {
                    @Override
                    public <V> void onRetry(Attempt<V> attempt) {
                        if (attempt.hasException()) {
                            RetryType retryType = retryerInfo.getRetryType();
                            switch (retryType) {
                                case ONLY_LOCAL:
                                case LOCAL_REMOTE:
                                    SnailJobLog.LOCAL.error("Local retry for [{}] failed, retry number [{}]", retryerInfo.getScene(), attempt.getAttemptNumber());
                                    break;
                                case ONLY_REMOTE:
                                    SnailJobLog.LOCAL.error("Report service end execution for [{}] failed, retry number [{}]", retryerInfo.getScene(), attempt.getAttemptNumber());
                                    break;
                                default:
                                    throw new SnailRetryClientException("Exception retry mode [{}]", retryType.name());

                            }
                        } else {
                            RetryType retryType = retryerInfo.getRetryType();
                            switch (retryType) {
                                case ONLY_LOCAL:
                                case LOCAL_REMOTE:
                                    SnailJobLog.LOCAL.info("Local retry for [{}] successful. retry number [{}]", retryerInfo.getScene(), attempt.getAttemptNumber());
                                    break;
                                case ONLY_REMOTE:
                                    SnailJobLog.LOCAL.info("Report service end execution for [{}] successful. retry number [{}]", retryerInfo.getScene(), attempt.getAttemptNumber());
                                    break;
                                default:
                                    throw new SnailRetryClientException("Exception retry mode [{}]. retry number [{}]", retryType.name(), attempt.getAttemptNumber());

                            }
                        }

                    }
                });
            }

        };
    }
}
