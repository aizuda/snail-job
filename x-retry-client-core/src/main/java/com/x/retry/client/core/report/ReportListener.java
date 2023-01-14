package com.x.retry.client.core.report;

import com.github.rholder.retry.*;
import com.google.common.base.Predicate;
import com.x.retry.client.core.RetryExecutor;
import com.x.retry.client.core.RetryExecutorParameter;
import com.x.retry.client.core.client.NettyHttpConnectClient;
import com.x.retry.client.core.client.request.ReportRetryInfoHttpRequestHandler;
import com.x.retry.client.core.client.request.RequestParam;
import com.x.retry.client.core.config.XRetryProperties;
import com.x.retry.common.core.model.XRetryRequest;
import com.x.retry.client.core.client.response.XRetryResponse;
import com.x.retry.client.core.executor.GuavaRetryExecutor;
import com.x.retry.common.core.context.SpringContext;
import com.x.retry.common.core.log.LogUtils;
import com.x.retry.common.core.util.JsonUtil;
import com.x.retry.common.core.window.Listener;
import com.x.retry.server.model.dto.RetryTaskDTO;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author: www.byteblogs.com
 * @date : 2022-03-08 13:54
 */
public class ReportListener implements Listener<RetryTaskDTO> {

    @Override
    public void handler(List<RetryTaskDTO> list) {
        RetryExecutor<WaitStrategy, StopStrategy> retryExecutor =
                new GuavaRetryExecutor();

        Retryer retryer = retryExecutor.build(getRetryExecutorParameter());

        try {
            retryExecutor.call(retryer, () -> {
                LogUtils.info("批量上报");

                XRetryRequest xRetryRequest = new XRetryRequest(list);
                ReportRetryInfoHttpRequestHandler requestHandler = SpringContext.getBeanByType(ReportRetryInfoHttpRequestHandler.class);
                XRetryResponse.cache(xRetryRequest, requestHandler.callable());
                NettyHttpConnectClient.send(requestHandler.method(), requestHandler.getHttpUrl(new RequestParam()), requestHandler.body(xRetryRequest));


                return null;
            }, throwable -> {
                LogUtils.info("上报重试后失败：{}", JsonUtil.toJsonString(list));
                // TODO 通知
            }, o -> LogUtils.info("上报重试成功：{}", JsonUtil.toJsonString(list)));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public RetryExecutorParameter<WaitStrategy, StopStrategy> getRetryExecutorParameter() {
        return new RetryExecutorParameter<WaitStrategy, StopStrategy>() {

            @Override
            public Predicate<Throwable> exceptionPredicate() {
                return throwable -> Boolean.TRUE;
            }

            @Override
            public WaitStrategy backOff() {
                return WaitStrategies.fixedWait(2, TimeUnit.SECONDS);
            }

            @Override
            public StopStrategy stop() {
                return StopStrategies.stopAfterAttempt(10);
            }

            @Override
            public List<RetryListener> getRetryListeners() {
                return Collections.singletonList(new RetryListener() {
                    @Override
                    public <V> void onRetry(Attempt<V> attempt) {
                        if (attempt.hasResult()) {
                            LogUtils.error("x-retry 上报成功，第[{}]次调度", attempt.getAttemptNumber());
                        }

                        if (attempt.hasException()) {
                            LogUtils.error("x-retry 上报失败，第[{}]次调度 ", attempt.getAttemptNumber(), attempt.getExceptionCause());
                        }

                    }
                });
            }

        };
    }
}
