package com.aizuda.easy.retry.client.core.strategy;

import com.aizuda.easy.retry.client.core.RetryExecutor;
import com.aizuda.easy.retry.client.core.RetryExecutorParameter;
import com.aizuda.easy.retry.client.core.exception.EasyRetryClientException;
import com.aizuda.easy.retry.client.core.intercepter.RetrySiteSnapshot;
import com.aizuda.easy.retry.client.core.report.ReportHandler;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * @author: www.byteblogs.com
 * @date : 2022-03-03 14:38
 */
@Component
@Slf4j
public class LocalRetryStrategies extends AbstractRetryStrategies {

    @Autowired
    private ReportHandler reportHandler;

    @Override
    public boolean supports(int stage, RetryType retryType) {
        return RetrySiteSnapshot.EnumStage.LOCAL.getStage() == stage ;
    }

    @Override
    protected void setStage() {
        RetrySiteSnapshot.setStage(RetrySiteSnapshot.EnumStage.LOCAL.getStage());
    }

    @Override
    protected Consumer<Object> doRetrySuccessConsumer(RetryerResultContext context) {
        return o -> {
            LogUtils.debug(log, "doRetrySuccessConsumer 重试成功");
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

        if (RetrySiteSnapshot.isRetryForStatusCode()) {
            resultContext.setRetryResultStatusEnum(RetryResultStatusEnum.FAILURE);
            resultContext.setMessage("执行重试检验不通过 原因: 下游标志禁止重试");
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
            log.info("内存重试完成且异常未被解决 scene:[{}]", retryerInfo.getScene());
            // 上报服务端异常
            if (RetryType.LOCAL_REMOTE.name().equals(retryerInfo.getRetryType().name())){
                // 上报
                log.debug("上报 scene:[{}]", retryerInfo.getScene());
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
                return () -> retryExecutor.execute(params);
            case ONLY_REMOTE:
                // 仅仅是远程重试则直接上报
                log.debug("上报 scene:[{}]", retryerInfo.getScene());
                return () -> {
                    doReport(retryerInfo, params);
                    RetrySiteSnapshot.setStage(RetrySiteSnapshot.EnumStage.REMOTE.getStage());
                    return null;
                };
            default:
                throw new EasyRetryClientException("异常重试模式 [{}]", retryType.name());
        }

    }

    /**
     * 上报数据
     *
     * @param retryerInfo 定义重试场景的信息
     * @param params 执行参数
     */
    private void doReport(final RetryerInfo retryerInfo, final Object[] params) {

        if (retryerInfo.isAsync()) {
            if (retryerInfo.isForceReport()) {
                reportHandler.asyncReportWithForce(retryerInfo.getScene(), retryerInfo.getExecutorClassName(), params);
            } else {
                reportHandler.asyncReport(retryerInfo.getScene(), retryerInfo.getExecutorClassName(), params);
            }
        } else {
            if (retryerInfo.isForceReport()) {
                reportHandler.syncReport(retryerInfo.getScene(), retryerInfo.getExecutorClassName(),
                    params, retryerInfo.getTimeout(), retryerInfo.getUnit());
            } else {
                reportHandler.syncReport(retryerInfo.getScene(), retryerInfo.getExecutorClassName(),
                    params, retryerInfo.getTimeout(), retryerInfo.getUnit());
            }
        }
    }

    @Override
    public RetryExecutorParameter<WaitStrategy, StopStrategy> getRetryExecutorParameter(RetryerInfo retryerInfo) {

        return new RetryExecutorParameter<WaitStrategy, StopStrategy>() {

            @Override
            public Predicate<Throwable> exceptionPredicate() {
                return throwable -> LocalRetryStrategies.super.validate(throwable.getClass(), retryerInfo);
            }

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
                        LogUtils.error(log,"easy-retry 本地重试，第[{}]次调度", attempt.getAttemptNumber());
                    }
                });
            }

        };
    }
}
