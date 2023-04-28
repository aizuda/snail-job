package com.aizuda.easy.retry.client.core.report;

import com.aizuda.easy.retry.client.core.RetryExecutor;
import com.aizuda.easy.retry.client.core.RetryExecutorParameter;
import com.aizuda.easy.retry.client.core.client.response.EasyRetryResponse;
import com.aizuda.easy.retry.client.core.config.EasyRetryProperties;
import com.github.rholder.retry.*;
import com.google.common.base.Predicate;
import com.aizuda.easy.retry.client.core.cache.GroupVersionCache;
import com.aizuda.easy.retry.client.core.client.NettyHttpConnectClient;
import com.aizuda.easy.retry.client.core.client.request.ReportRetryInfoHttpRequestHandler;
import com.aizuda.easy.retry.client.core.client.request.RequestParam;
import com.aizuda.easy.retry.client.core.executor.GuavaRetryExecutor;
import com.aizuda.easy.retry.common.core.alarm.Alarm;
import com.aizuda.easy.retry.common.core.alarm.AlarmContext;
import com.aizuda.easy.retry.common.core.alarm.AltinAlarmFactory;
import com.aizuda.easy.retry.common.core.context.SpringContext;
import com.aizuda.easy.retry.common.core.enums.NotifySceneEnum;
import com.aizuda.easy.retry.common.core.log.LogUtils;
import com.aizuda.easy.retry.common.core.model.XRetryRequest;
import com.aizuda.easy.retry.common.core.util.EnvironmentUtils;
import com.aizuda.easy.retry.common.core.util.JsonUtil;
import com.aizuda.easy.retry.common.core.window.Listener;
import com.aizuda.easy.retry.server.model.dto.ConfigDTO;
import com.aizuda.easy.retry.server.model.dto.RetryTaskDTO;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * @author: www.byteblogs.com
 * @date : 2022-03-08 13:54
 */
@Slf4j
public class ReportListener implements Listener<RetryTaskDTO> {
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static String reportErrorTextMessageFormatter =
            "<font face=\"微软雅黑\" color=#ff0000 size=4>{}环境 上报失败</font>  \r\n" +
                    "> 名称:{}  \r\n" +
                    "> 时间:{}  \r\n" +
                    "> 异常:{}  \n"
            ;

    @Override
    public void handler(List<RetryTaskDTO> list) {
        RetryExecutor<WaitStrategy, StopStrategy> retryExecutor =
                new GuavaRetryExecutor();

        Retryer retryer = retryExecutor.build(getRetryExecutorParameter());

        try {
            retryExecutor.call(retryer, () -> {
                LogUtils.info(log, "批量上报");

                XRetryRequest xRetryRequest = new XRetryRequest(list);
                ReportRetryInfoHttpRequestHandler requestHandler = SpringContext.getBeanByType(ReportRetryInfoHttpRequestHandler.class);
                EasyRetryResponse.cache(xRetryRequest, requestHandler.callable());
                NettyHttpConnectClient.send(requestHandler.method(), requestHandler.getHttpUrl(new RequestParam()), requestHandler.body(xRetryRequest));

                return null;
            }, throwable -> {
                LogUtils.info(log,"上报重试后失败：{}", JsonUtil.toJsonString(list));
                sendMessage(throwable);
            }, o -> LogUtils.info(log,"上报重试成功：{}", JsonUtil.toJsonString(list)));
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
                            LogUtils.error(log,"easy-retry 上报成功，第[{}]次调度", attempt.getAttemptNumber());
                        }

                        if (attempt.hasException()) {
                            LogUtils.error(log,"easy-retry 上报失败，第[{}]次调度 ", attempt.getAttemptNumber(), attempt.getExceptionCause());
                        }

                    }
                });
            }

        };
    }

    private void sendMessage(Throwable e) {

        try {
            ConfigDTO.Notify notifyAttribute = GroupVersionCache.getNotifyAttribute(NotifySceneEnum.CLIENT_REPORT_ERROR.getNotifyScene());
            if (Objects.nonNull(notifyAttribute)) {
                AlarmContext context = AlarmContext.build()
                        .text(reportErrorTextMessageFormatter,
                                EnvironmentUtils.getActiveProfile(),
                                EasyRetryProperties.getGroup(),
                                LocalDateTime.now().format(formatter),
                                e.getMessage())
                        .title("上报异常:[{}]", EasyRetryProperties.getGroup())
                        .notifyAttribute(notifyAttribute.getNotifyAttribute());

                AltinAlarmFactory altinAlarmFactory = SpringContext.getBeanByType(AltinAlarmFactory.class);
                Alarm<AlarmContext> alarmType = altinAlarmFactory.getAlarmType(notifyAttribute.getNotifyType());
                alarmType.asyncSendMessage(context);
            }
        } catch (Exception e1) {
            LogUtils.error(log, "客户端发送组件异常告警失败", e1);
        }

    }
}
