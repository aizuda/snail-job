package com.aizuda.snailjob.client.core.report;

import com.aizuda.snailjob.client.common.NettyClient;
import com.aizuda.snailjob.client.common.cache.GroupVersionCache;
import com.aizuda.snailjob.client.common.config.SnailJobProperties;
import com.aizuda.snailjob.client.common.rpc.client.RequestBuilder;
import com.aizuda.snailjob.client.core.RetryExecutor;
import com.aizuda.snailjob.client.core.RetryExecutorParameter;
import com.aizuda.snailjob.client.core.executor.GuavaRetryExecutor;
import com.aizuda.snailjob.common.core.alarm.AlarmContext;
import com.aizuda.snailjob.common.core.alarm.SnailJobAlarmFactory;
import com.aizuda.snailjob.common.core.context.SnailSpringContext;
import com.aizuda.snailjob.common.core.enums.RetryNotifySceneEnum;
import com.aizuda.snailjob.common.core.model.SnailJobRpcResult;
import com.aizuda.snailjob.common.core.util.EnvironmentUtils;
import com.aizuda.snailjob.common.core.util.JsonUtil;
import com.aizuda.snailjob.common.core.util.NetUtil;
import com.aizuda.snailjob.common.core.window.Listener;
import com.aizuda.snailjob.common.log.SnailJobLog;
import com.aizuda.snailjob.server.model.dto.ConfigDTO;
import com.aizuda.snailjob.server.model.dto.ConfigDTO.Notify.Recipient;
import com.aizuda.snailjob.server.model.dto.RetryTaskDTO;
import com.github.rholder.retry.*;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static com.aizuda.snailjob.common.core.constant.SystemConstants.YYYY_MM_DD_HH_MM_SS;

/**
 * 批量异步上报
 *
 * @author: opensnail
 * @date : 2022-03-08 13:54
 * @since 1.0.0
 */
@Slf4j
public class ReportListener implements Listener<RetryTaskDTO> {
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(YYYY_MM_DD_HH_MM_SS);
    private static final String reportErrorTextMessageFormatter =
            "<font face=\"微软雅黑\" color=#ff0000 size=4>{}环境 异步批量上报异常</font>  \n" +
                    "> IP:{}  \n" +
                    "> 空间ID:{}  \n" +
                    "> 名称:{}  \n" +
                    "> 时间:{}  \n" +
                    "> 异常:{}  \n";

    private static final NettyClient CLIENT = RequestBuilder.<NettyClient, SnailJobRpcResult>newBuilder()
            .client(NettyClient.class)
            .callback(nettyResult -> SnailJobLog.LOCAL.info("Data report successfully requestId:[{}]", nettyResult.getReqId())).build();

    @Override
    public void handler(List<RetryTaskDTO> list) {
        RetryExecutor<WaitStrategy, StopStrategy> retryExecutor =
                new GuavaRetryExecutor();

        Retryer retryer = retryExecutor.build(getRetryExecutorParameter());

        try {
            retryExecutor.call(retryer, () -> {
                SnailJobLog.LOCAL.info("Batch asynchronous reporting ... <|>{}<|>", JsonUtil.toJsonString(list));
                CLIENT.reportRetryInfo(list);
                return null;
            }, throwable -> {
                SnailJobLog.LOCAL.error("Data report failed. <|>{}<|>", JsonUtil.toJsonString(list));
                sendMessage(throwable);
            }, o -> SnailJobLog.LOCAL.info("Data report successful retry：<|>{}<|>", JsonUtil.toJsonString(list)));
        } catch (Exception e) {
            SnailJobLog.LOCAL.error("Data report failed. <|>{}<|>", JsonUtil.toJsonString(list), e);
        }
    }

    public RetryExecutorParameter<WaitStrategy, StopStrategy> getRetryExecutorParameter() {
        return new RetryExecutorParameter<WaitStrategy, StopStrategy>() {

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

                        if (attempt.hasException()) {
                            SnailJobLog.LOCAL.error("SnailJob上报异常数据时接口发生异常，第[{}]次尝试上报 ", attempt.getAttemptNumber(), attempt.getExceptionCause());
                        }

                    }
                });
            }

        };
    }

    private void sendMessage(Throwable e) {

        try {
            ConfigDTO.Notify notify = GroupVersionCache.getRetryNotifyAttribute(RetryNotifySceneEnum.CLIENT_REPORT_ERROR.getNotifyScene());
            if (Objects.isNull(notify)) {
                return;
            }

            SnailJobProperties properties = SnailSpringContext.getBean(SnailJobProperties.class);
            if (Objects.isNull(properties)) {
                return;
            }
            List<Recipient> recipients = Optional.ofNullable(notify.getRecipients()).orElse(Lists.newArrayList());
            for (final Recipient recipient : recipients) {
                AlarmContext context = AlarmContext.build()
                        .text(reportErrorTextMessageFormatter,
                                EnvironmentUtils.getActiveProfile(),
                                NetUtil.getLocalIpStr(),
                                properties.getNamespace(),
                                properties.getGroup(),
                                LocalDateTime.now().format(formatter),
                                e.getMessage())
                        .title("上报异常:[{}]", properties.getGroup())
                        .notifyAttribute(recipient.getNotifyAttribute());
                Optional.ofNullable(SnailJobAlarmFactory.getAlarmType(recipient.getNotifyType())).ifPresent(alarm -> alarm.asyncSendMessage(context));
            }

        } catch (Exception e1) {
            SnailJobLog.LOCAL.error("客户端发送组件异常告警失败", e1);
        }

    }
}
