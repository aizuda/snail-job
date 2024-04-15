package com.aizuda.snail.job.client.core.report;

import com.aizuda.snail.job.client.common.config.SnailJobProperties;
import com.aizuda.snail.job.client.common.rpc.client.RequestBuilder;
import com.aizuda.snail.job.client.core.RetryExecutor;
import com.aizuda.snail.job.client.core.RetryExecutorParameter;
import com.aizuda.snail.job.client.common.cache.GroupVersionCache;
import com.aizuda.snail.job.client.common.client.NettyClient;
import com.aizuda.snail.job.client.core.executor.GuavaRetryExecutor;
import com.aizuda.snail.job.common.core.alarm.Alarm;
import com.aizuda.snail.job.common.core.alarm.AlarmContext;
import com.aizuda.snail.job.common.core.alarm.EasyRetryAlarmFactory;
import com.aizuda.snail.job.common.core.context.SpringContext;
import com.aizuda.snail.job.common.core.enums.NotifySceneEnum;
import com.aizuda.snail.job.common.log.EasyRetryLog;
import com.aizuda.snail.job.common.core.model.NettyResult;
import com.aizuda.snail.job.common.core.util.EnvironmentUtils;
import com.aizuda.snail.job.common.core.util.NetUtil;
import com.aizuda.snail.job.common.core.util.JsonUtil;
import com.aizuda.snail.job.common.core.window.Listener;
import com.aizuda.snail.job.server.model.dto.ConfigDTO;
import com.aizuda.snail.job.server.model.dto.RetryTaskDTO;
import com.github.rholder.retry.*;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * 批量异步上报
 *
 * @author: opensnail
 * @date : 2022-03-08 13:54
 * @since 1.0.0
 */
@Slf4j
public class ReportListener implements Listener<RetryTaskDTO> {
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static String reportErrorTextMessageFormatter =
            "<font face=\"微软雅黑\" color=#ff0000 size=4>{}环境 异步批量上报异常</font>  \n" +
                    "> IP:{}  \n" +
                    "> 空间ID:{}  \n" +
                    "> 名称:{}  \n" +
                    "> 时间:{}  \n" +
                    "> 异常:{}  \n"
            ;

    private static final NettyClient CLIENT = RequestBuilder.<NettyClient, NettyResult>newBuilder()
        .client(NettyClient.class)
        .callback(nettyResult ->EasyRetryLog.LOCAL.info("Data report successfully requestId:[{}]", nettyResult.getRequestId())).build();

    @Override
    public void handler(List<RetryTaskDTO> list) {
        RetryExecutor<WaitStrategy, StopStrategy> retryExecutor =
                new GuavaRetryExecutor();

        Retryer retryer = retryExecutor.build(getRetryExecutorParameter());

        try {
            retryExecutor.call(retryer, () -> {
               EasyRetryLog.LOCAL.info("Batch asynchronous reporting ... <|>{}<|>", JsonUtil.toJsonString(list));
                CLIENT.reportRetryInfo(list);
                return null;
            }, throwable -> {
                EasyRetryLog.LOCAL.error("Data report failed. <|>{}<|>", JsonUtil.toJsonString(list));
                sendMessage(throwable);
            }, o ->EasyRetryLog.LOCAL.info("Data report successful retry：<|>{}<|>", JsonUtil.toJsonString(list)));
        } catch (Exception e) {
            EasyRetryLog.LOCAL.error("Data report failed. <|>{}<|>", JsonUtil.toJsonString(list), e);
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
                            EasyRetryLog.LOCAL.error("EasyRetry上报异常数据时接口发生异常，第[{}]次尝试上报 ", attempt.getAttemptNumber(), attempt.getExceptionCause());
                        }

                    }
                });
            }

        };
    }

    private void sendMessage(Throwable e) {

        try {
            ConfigDTO.Notify notifyAttribute = GroupVersionCache.getNotifyAttribute(NotifySceneEnum.CLIENT_REPORT_ERROR.getNotifyScene());
            if (Objects.isNull(notifyAttribute)) {
                return;
            }

            SnailJobProperties properties = SpringContext.getBean(SnailJobProperties.class);
            AlarmContext context = AlarmContext.build()
                    .text(reportErrorTextMessageFormatter,
                            EnvironmentUtils.getActiveProfile(),
                            NetUtil.getLocalIpStr(),
                            properties.getNamespace(),
                            SnailJobProperties.getGroup(),
                            LocalDateTime.now().format(formatter),
                            e.getMessage())
                    .title("上报异常:[{}]", SnailJobProperties.getGroup())
                    .notifyAttribute(notifyAttribute.getNotifyAttribute());

            EasyRetryAlarmFactory easyRetryAlarmFactory = SpringContext.getBeanByType(EasyRetryAlarmFactory.class);
            Alarm<AlarmContext> alarmType = easyRetryAlarmFactory.getAlarmType(notifyAttribute.getNotifyType());
            alarmType.asyncSendMessage(context);
        } catch (Exception e1) {
            EasyRetryLog.LOCAL.error("客户端发送组件异常告警失败", e1);
        }

    }
}
