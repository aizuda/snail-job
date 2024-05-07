package com.aizuda.snailjob.server.common.alarm;

import com.aizuda.snailjob.common.core.alarm.Alarm;
import com.aizuda.snailjob.common.core.alarm.AlarmContext;
import com.aizuda.snailjob.common.core.alarm.SnailJobAlarmFactory;
import com.aizuda.snailjob.common.core.enums.StatusEnum;
import com.aizuda.snailjob.common.core.util.JsonUtil;
import com.aizuda.snailjob.common.log.SnailJobLog;
import com.aizuda.snailjob.server.common.AlarmInfoConverter;
import com.aizuda.snailjob.server.common.Lifecycle;
import com.aizuda.snailjob.server.common.cache.CacheNotifyRateLimiter;
import com.aizuda.snailjob.server.common.dto.AlarmInfo;
import com.aizuda.snailjob.server.common.dto.NotifyConfigInfo;
import com.aizuda.snailjob.server.common.dto.NotifyConfigInfo.RecipientInfo;
import com.aizuda.snailjob.server.common.enums.SyetemTaskTypeEnum;
import com.aizuda.snailjob.server.common.triple.ImmutableTriple;
import com.aizuda.snailjob.server.common.triple.Triple;
import com.aizuda.snailjob.template.datasource.access.AccessTemplate;
import com.aizuda.snailjob.template.datasource.persistence.mapper.NotifyRecipientMapper;
import com.aizuda.snailjob.template.datasource.persistence.po.NotifyConfig;
import com.aizuda.snailjob.template.datasource.persistence.po.NotifyRecipient;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.util.concurrent.RateLimiter;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalApplicationListener;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.util.CollectionUtils;

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
            if (CollectionUtils.isEmpty(alarmInfos)) {
                return;
            }

            // 获取所有的命名空间
            Set<String> namespaceIds = new HashSet<>();
            // 获取所有的组名称
            Set<String> groupNames = new HashSet<>();
            // 获取所有的场景名称
            Set<String> businessIds = new HashSet<>();

            // 转换AlarmDTO 为了下面循环发送使用
            Map<Triple<String, String, String>, List<A>> waitSendAlarmInfos = convertAlarmDTO(
                alarmInfos, namespaceIds, groupNames, businessIds);

            // 批量获取通知配置
            Map<Triple<String, String, String>, List<NotifyConfigInfo>> notifyConfig = obtainNotifyConfig(namespaceIds,
                groupNames, businessIds);

            // 循环发送消息
            waitSendAlarmInfos.forEach((key, list) -> {
                List<NotifyConfigInfo> notifyConfigsList = notifyConfig.getOrDefault(key, Lists.newArrayList());
                for (A alarmDTO : list) {
                    sendAlarm(notifyConfigsList, alarmDTO);
                }
            });
        } catch (InterruptedException e) {
            SnailJobLog.LOCAL.info("retry task fail dead letter alarm stop");
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            SnailJobLog.LOCAL.error("RetryTaskFailDeadLetterAlarmListener queue poll Exception", e);
        }

    }

    protected Map<Triple<String, String, String>, List<NotifyConfigInfo>> obtainNotifyConfig(Set<String> namespaceIds,
        Set<String> groupNames, Set<String> businessIds) {

        // 批量获取所需的通知配置
        List<NotifyConfig> notifyConfigs = accessTemplate.getNotifyConfigAccess().list(
            new LambdaQueryWrapper<NotifyConfig>()
                .eq(NotifyConfig::getNotifyStatus, StatusEnum.YES.getStatus())
                .in(NotifyConfig::getSystemTaskType, getSystemTaskType().stream().map(i -> i.getType()).collect(Collectors.toList()))
                .eq(NotifyConfig::getNotifyScene, getNotifyScene())
                .in(NotifyConfig::getNamespaceId, namespaceIds)
                .in(NotifyConfig::getGroupName, groupNames)
                .in(NotifyConfig::getBusinessId, businessIds)
        );
        if (CollectionUtils.isEmpty(notifyConfigs)) {
            return Maps.newHashMap();
        }

        Set<Long> recipientIds = notifyConfigs.stream()
            .map(config -> {
                return new HashSet<>(JsonUtil.parseList(config.getRecipientIds(), Long.class));
            })
            .reduce((a, b) -> {
                HashSet<Long> set = Sets.newHashSet();
                set.addAll(a);
                set.addAll(b);
                return set;
            }).orElse(new HashSet<>());

        List<NotifyRecipient> notifyRecipients = recipientMapper.selectBatchIds(recipientIds);
        Map<Long, NotifyRecipient> recipientMap = notifyRecipients.stream()
            .collect(Collectors.toMap(NotifyRecipient::getId, i -> i));

        if (CollectionUtils.isEmpty(recipientIds)) {
            return Maps.newHashMap();
        }

        List<NotifyConfigInfo> notifyConfigInfos = AlarmInfoConverter.INSTANCE.retryToNotifyConfigInfos(notifyConfigs);

        return notifyConfigInfos.stream()
            .collect(Collectors.groupingBy(i -> {

                List<RecipientInfo> recipients = i.getRecipientIds().stream().map(recipientId -> {
                    NotifyRecipient notifyRecipient = recipientMap.get(recipientId);
                    if (Objects.isNull(notifyRecipient)) {
                        return null;
                    }

                    RecipientInfo notifyConfigInfo = new RecipientInfo();
                    notifyConfigInfo.setNotifyAttribute(notifyRecipient.getNotifyAttribute());
                    notifyConfigInfo.setNotifyType(notifyRecipient.getNotifyType());
                    return notifyConfigInfo;
                }).collect(Collectors.toList());
                i.setRecipientInfos(recipients);

                return ImmutableTriple.of(i.getNamespaceId(), i.getGroupName(), i.getBusinessId());
            }));

    }

    protected abstract List<SyetemTaskTypeEnum> getSystemTaskType();

    protected abstract Map<Triple<String, String, String>, List<A>> convertAlarmDTO(List<A> alarmData,
        Set<String> namespaceIds, Set<String> groupNames, Set<String> sceneNames);

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

    protected void sendAlarm(List<NotifyConfigInfo> notifyConfigsList, A alarmDTO) {
        for (final NotifyConfigInfo notifyConfig : notifyConfigsList) {
            if (Objects.equals(notifyConfig.getRateLimiterStatus(), StatusEnum.YES.getStatus())) {
                // 限流
                RateLimiter rateLimiter = getRateLimiter(String.valueOf(notifyConfig.getId()),
                    notifyConfig.getRateLimiterThreshold());
                // 每秒发送rateLimiterThreshold个告警
                if (Objects.nonNull(rateLimiter) && !rateLimiter.tryAcquire(1, TimeUnit.SECONDS)) {
                    continue;
                }
            }

            if (Objects.nonNull(alarmDTO.getCount()) && Objects.nonNull(notifyConfig.getNotifyThreshold())) {
                if (notifyConfig.getNotifyThreshold() >= alarmDTO.getCount()) {
                    continue;
                }
            }

            for (final RecipientInfo recipientInfo : notifyConfig.getRecipientInfos()) {
                if (Objects.isNull(recipientInfo)) {
                    continue;
                }
                AlarmContext context = buildAlarmContext(alarmDTO, notifyConfig);
                context.setNotifyAttribute(recipientInfo.getNotifyAttribute());
                Alarm<AlarmContext> alarm = SnailJobAlarmFactory.getAlarmType(
                    recipientInfo.getNotifyType());
                alarm.asyncSendMessage(context);
            }

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

    @TransactionalEventListener(fallbackExecution = true, phase = TransactionPhase.AFTER_COMPLETION)
    public void onApplicationEvent(@NotNull E event) {
        doOnApplicationEvent(event);
    }

    protected abstract void doOnApplicationEvent(E event);
}


