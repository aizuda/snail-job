package com.aizuda.easy.retry.server.common.alarm;

import com.aizuda.easy.retry.common.core.alarm.Alarm;
import com.aizuda.easy.retry.common.core.alarm.AlarmContext;
import com.aizuda.easy.retry.common.core.alarm.EasyRetryAlarmFactory;
import com.aizuda.easy.retry.common.core.enums.StatusEnum;
import com.aizuda.easy.retry.common.core.log.LogUtils;
import com.aizuda.easy.retry.server.common.Lifecycle;
import com.aizuda.easy.retry.server.common.dto.AlarmInfo;
import com.aizuda.easy.retry.server.common.dto.NotifyConfigInfo;
import com.aizuda.easy.retry.server.common.triple.Triple;
import com.aizuda.easy.retry.template.datasource.access.AccessTemplate;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.RateLimiter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @author xiaowoniu
 * @date 2023-12-03 09:47:11
 * @since 2.5.0
 */
@Slf4j
public abstract class AbstractAlarm<E extends ApplicationEvent, A extends AlarmInfo, T> extends AbstractFlowControl<E> implements ApplicationListener<E>, Runnable, Lifecycle {

    @Autowired
    private EasyRetryAlarmFactory easyRetryAlarmFactory;

    @Autowired
    protected AccessTemplate accessTemplate;

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {

            try {
                // 从队列获取数据
                List<A> alarmInfos = poll();
                if (CollectionUtils.isEmpty(alarmInfos)) {
                    continue;
                }

                // 获取所有的命名空间
                Set<String> namespaceIds = new HashSet<>();
                // 获取所有的组名称
                Set<String> groupNames = new HashSet<>();
                // 获取所有的场景名称
                Set<T> sceneNames = new HashSet<>();

                // 转换AlarmDTO 为了下面循环发送使用
                Map<Triple<String, String, T>, List<A>> waitSendAlarmInfos = convertAlarmDTO(
                        alarmInfos, namespaceIds, groupNames, sceneNames);

                // 批量获取通知配置
                Map<Triple<String, String, T>, List<NotifyConfigInfo>> notifyConfig = obtainNotifyConfig(namespaceIds, groupNames, sceneNames);

                // 循环发送消息
                waitSendAlarmInfos.forEach((key, list) -> {
                    List<NotifyConfigInfo> notifyConfigsList = notifyConfig.getOrDefault(key, Lists.newArrayList());
                    for (A alarmDTO : list) {
                        sendAlarm(notifyConfigsList, alarmDTO);
                    }
                });
            } catch (InterruptedException e) {
                LogUtils.info(log, "retry task fail dead letter alarm stop");
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                LogUtils.error(log, "RetryTaskFailDeadLetterAlarmListener queue poll Exception", e);
            }
        }
    }

    protected abstract Map<Triple<String, String, T>, List<A>> convertAlarmDTO(List<A> alarmData, Set<String> namespaceIds, Set<String> groupNames, Set<T> sceneNames);

    protected abstract Map<Triple<String/*命名空间*/, String/*组名称*/, T/*场景名称ORJobId*/>,
            List<NotifyConfigInfo>> obtainNotifyConfig(Set<String> namespaceIds,
                                                       Set<String> groupNames,
                                                       Set<T> sceneNames);

    protected abstract List<A> poll() throws InterruptedException;

    protected abstract AlarmContext buildAlarmContext(A alarmDTO, NotifyConfigInfo notifyConfig);

    private Thread thread;

    @Override
    public void start() {
        thread = new Thread(this);
        thread.start();
        startLog();
    }

    protected abstract void startLog();

    @Override
    public void close() {
        if (Objects.nonNull(thread)) {
            thread.interrupt();
        }
    }

    protected void sendAlarm(List<NotifyConfigInfo> notifyConfigsList, A alarmDTO) {
        for (final NotifyConfigInfo notifyConfig : notifyConfigsList) {
            if (Objects.equals(notifyConfig.getRateLimiterStatus(), StatusEnum.YES.getStatus())) {
                // 限流
                RateLimiter rateLimiter = getRateLimiter(rateLimiterKey(notifyConfig),
                        notifyConfig.getRateLimiterThreshold());
                // 每秒发送rateLimiterThreshold个告警
                if (Objects.nonNull(rateLimiter) && !RateLimiter.create(notifyConfig.getRateLimiterThreshold())
                        .tryAcquire(1, TimeUnit.SECONDS)) {
                    continue;
                }
            }

            AlarmContext context = buildAlarmContext(alarmDTO, notifyConfig);
            Alarm<AlarmContext> alarmType = easyRetryAlarmFactory.getAlarmType(
                    notifyConfig.getNotifyType());
            alarmType.asyncSendMessage(context);
        }
    }

    protected abstract String rateLimiterKey(NotifyConfigInfo notifyConfig);

    protected abstract int getNotifyScene();
}


