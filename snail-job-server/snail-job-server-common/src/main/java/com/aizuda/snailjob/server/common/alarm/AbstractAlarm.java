package com.aizuda.snailjob.server.common.alarm;

import cn.hutool.core.collection.CollUtil;
import com.aizuda.snailjob.common.core.alarm.Alarm;
import com.aizuda.snailjob.common.core.alarm.AlarmContext;
import com.aizuda.snailjob.common.core.alarm.SnailJobAlarmFactory;
import com.aizuda.snailjob.common.core.enums.StatusEnum;
import com.aizuda.snailjob.common.core.util.JsonUtil;
import com.aizuda.snailjob.common.core.util.StreamUtils;
import com.aizuda.snailjob.common.log.SnailJobLog;
import com.aizuda.snailjob.server.common.AlarmInfoConverter;
import com.aizuda.snailjob.server.common.Lifecycle;
import com.aizuda.snailjob.server.common.cache.CacheNotifyRateLimiter;
import com.aizuda.snailjob.server.common.dto.AlarmInfo;
import com.aizuda.snailjob.server.common.dto.NotifyConfigInfo;
import com.aizuda.snailjob.server.common.dto.NotifyConfigInfo.RecipientInfo;
import com.aizuda.snailjob.server.common.enums.SyetemTaskTypeEnum;
import com.aizuda.snailjob.template.datasource.access.AccessTemplate;
import com.aizuda.snailjob.template.datasource.persistence.mapper.NotifyRecipientMapper;
import com.aizuda.snailjob.template.datasource.persistence.po.NotifyConfig;
import com.aizuda.snailjob.template.datasource.persistence.po.NotifyRecipient;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.RateLimiter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEvent;
import org.springframework.scheduling.TaskScheduler;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author xiaowoniu
 * @date 2023-12-03 09:47:11
 * @since 2.5.0
 */
@Slf4j
public abstract class AbstractAlarm<E extends ApplicationEvent, A extends AlarmInfo> implements
        Runnable,
        Lifecycle {

    @Autowired
    @Qualifier("alarmExecutorService")
    protected TaskScheduler taskScheduler;
    @Autowired
    protected AccessTemplate accessTemplate;
    @Autowired
    protected NotifyRecipientMapper recipientMapper;

    @Override
    public void run() {

        try {
            // 从队列获取数据
            List<A> alarmInfos = poll();
            if (CollUtil.isEmpty(alarmInfos)) {
                return;
            }

            // 通知场景
            Set<Integer> notifyScene = new HashSet<>();

            // 转换AlarmDTO 为了下面循环发送使用
            Map<Long, List<A>> waitSendAlarmInfos = convertAlarmDTO(alarmInfos, notifyScene);
            Set<Long> notifyIds = waitSendAlarmInfos.keySet();

            // 批量获取通知配置
            Map<Long, NotifyConfigInfo> notifyConfigMap = obtainNotifyConfig(notifyScene, notifyIds);

            // 循环发送消息
            waitSendAlarmInfos.forEach((key, list) -> {
                Optional.ofNullable(notifyConfigMap.get(key)).ifPresent(notifyConfig -> {
                    for (A alarmDTO : list) {
                        sendAlarm(notifyConfig, alarmDTO);
                    }
                });
            });
        } catch (InterruptedException e) {
            SnailJobLog.LOCAL.info("retry task fail dead letter alarm stop");
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            SnailJobLog.LOCAL.error("RetryTaskFailDeadLetterAlarmListener queue poll Exception", e);
        }
    }

    protected Map<Long, NotifyConfigInfo> obtainNotifyConfig(Set<Integer> notifyScene,
                                                             Set<Long> notifyIds) {
        if (CollUtil.isEmpty(notifyIds) || CollUtil.isEmpty(notifyScene)) {
            return Maps.newHashMap();
        }

        // 批量获取所需的通知配置
        List<NotifyConfig> notifyConfigs = accessTemplate.getNotifyConfigAccess().list(
                new LambdaQueryWrapper<NotifyConfig>()
                        .eq(NotifyConfig::getNotifyStatus, StatusEnum.YES.getStatus())
                        .in(NotifyConfig::getNotifyScene, notifyScene)
                        .in(NotifyConfig::getSystemTaskType, StreamUtils.toList(getSystemTaskType(), SyetemTaskTypeEnum::getType))
                        .in(NotifyConfig::getId, notifyIds)
        );
        if (CollUtil.isEmpty(notifyConfigs)) {
            return Maps.newHashMap();
        }

        Set<Long> recipientIds = notifyConfigs.stream()
                .flatMap(config -> JsonUtil.parseList(config.getRecipientIds(), Long.class).stream())
                .collect(Collectors.toSet());

        List<NotifyRecipient> notifyRecipients = recipientMapper.selectByIds(recipientIds);
        Map<Long, NotifyRecipient> recipientMap = StreamUtils.toIdentityMap(notifyRecipients, NotifyRecipient::getId);

        if (CollUtil.isEmpty(recipientIds)) {
            return Maps.newHashMap();
        }

        List<NotifyConfigInfo> notifyConfigInfos = AlarmInfoConverter.INSTANCE.retryToNotifyConfigInfos(notifyConfigs);
        for (NotifyConfigInfo notifyConfigInfo : notifyConfigInfos) {
            List<RecipientInfo> recipients = StreamUtils.toList(notifyConfigInfo.getRecipientIds(), recipientId -> {
                NotifyRecipient notifyRecipient = recipientMap.get(recipientId);
                if (Objects.isNull(notifyRecipient)) {
                    return null;
                }

                RecipientInfo recipientInfo = new RecipientInfo();
                recipientInfo.setNotifyAttribute(notifyRecipient.getNotifyAttribute());
                recipientInfo.setNotifyType(notifyRecipient.getNotifyType());
                return recipientInfo;
            });
            notifyConfigInfo.setRecipientInfos(recipients);
        }

        return StreamUtils.toIdentityMap(notifyConfigInfos, NotifyConfigInfo::getId);
    }

    protected abstract List<SyetemTaskTypeEnum> getSystemTaskType();

    protected abstract Map<Long, List<A>> convertAlarmDTO(List<A> alarmData, Set<Integer> notifyScene);

    protected abstract List<A> poll() throws InterruptedException;

    protected abstract AlarmContext buildAlarmContext(A alarmDTO, NotifyConfigInfo notifyConfig);

    @Override
    public void start() {
        startLog();
        taskScheduler.scheduleAtFixedRate(this, Duration.parse("PT1S"));
    }

    protected abstract void startLog();

    @Override
    public void close() {
    }

    protected void sendAlarm(NotifyConfigInfo notifyConfig, A alarmDTO) {
        if (Objects.equals(notifyConfig.getRateLimiterStatus(), StatusEnum.YES.getStatus())) {
            // 限流
            RateLimiter rateLimiter = getRateLimiter(String.valueOf(notifyConfig.getId()), notifyConfig.getRateLimiterThreshold());
            // 每秒发送rateLimiterThreshold个告警
            if (Objects.nonNull(rateLimiter) && !rateLimiter.tryAcquire(1, TimeUnit.SECONDS)) {
                return;
            }
        }

        // 重试通知阈值
        if (Objects.nonNull(alarmDTO.getCount())
                && Objects.nonNull(notifyConfig.getNotifyThreshold())
                && alarmDTO.getCount() < notifyConfig.getNotifyThreshold()) {
            return;
        }

        for (final RecipientInfo recipientInfo : notifyConfig.getRecipientInfos()) {
            if (Objects.isNull(recipientInfo)) {
                continue;
            }
            AlarmContext context = buildAlarmContext(alarmDTO, notifyConfig);
            context.setNotifyAttribute(recipientInfo.getNotifyAttribute());
            Alarm<AlarmContext> alarm = SnailJobAlarmFactory.getAlarmType(recipientInfo.getNotifyType());
            alarm.asyncSendMessage(context);
        }

    }

    protected RateLimiter getRateLimiter(String key, double rateLimiterThreshold) {
        RateLimiter rateLimiter = CacheNotifyRateLimiter.getRateLimiterByKey(key);
        if (Objects.isNull(rateLimiter) || rateLimiter.getRate() != rateLimiterThreshold) {
            CacheNotifyRateLimiter.put(key, RateLimiter.create(rateLimiterThreshold));
        }

        return rateLimiter;
    }

    protected abstract int getNotifyScene();

    protected abstract void doOnApplicationEvent(E event);
}
