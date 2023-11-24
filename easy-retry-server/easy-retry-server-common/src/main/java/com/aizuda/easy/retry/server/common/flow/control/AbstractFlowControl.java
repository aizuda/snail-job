package com.aizuda.easy.retry.server.common.flow.control;

import com.aizuda.easy.retry.common.core.alarm.Alarm;
import com.aizuda.easy.retry.common.core.alarm.AlarmContext;
import com.aizuda.easy.retry.common.core.alarm.EasyRetryAlarmFactory;
import com.aizuda.easy.retry.common.core.enums.NotifySceneEnum;
import com.aizuda.easy.retry.common.core.enums.StatusEnum;
import com.aizuda.easy.retry.common.core.log.LogUtils;
import com.aizuda.easy.retry.server.common.cache.CacheNotifyRateLimiter;
import com.aizuda.easy.retry.server.common.triple.ImmutableTriple;
import com.aizuda.easy.retry.server.common.triple.Triple;
import com.aizuda.easy.retry.template.datasource.access.AccessTemplate;
import com.aizuda.easy.retry.template.datasource.persistence.po.NotifyConfig;
import com.aizuda.easy.retry.template.datasource.persistence.po.RetryDeadLetter;
import com.aizuda.easy.retry.template.datasource.persistence.po.RetryTask;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.common.util.concurrent.RateLimiter;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


/**
 * @author: zuoJunLin
 * @date : 2023-11-21 13:04
 * @since 2.5.0
 */
@Slf4j
public abstract class AbstractFlowControl<E extends ApplicationEvent> implements ApplicationListener<E> {


    @Autowired
    private EasyRetryAlarmFactory easyRetryAlarmFactory;

    @Autowired
    protected AccessTemplate accessTemplate;

    protected RateLimiter getRateLimiter(String key, double rateLimiterThreshold) {
        RateLimiter rateLimiter = CacheNotifyRateLimiter.getRateLimiterByKey(key);
        if (Objects.isNull(rateLimiter) || rateLimiter.getRate() != rateLimiterThreshold) {
            CacheNotifyRateLimiter.put(key, RateLimiter.create(rateLimiterThreshold));
        }

        return rateLimiter;
    }

    protected Map<Triple<String, String, String>, List<NotifyConfig>> getNotifyConfigMap(final Set<String> namespaceIds,
        final Set<String> groupNames, final Set<String> sceneNames) {

        // 批量获取所需的通知配置
        List<NotifyConfig> notifyConfigs = accessTemplate.getNotifyConfigAccess().list(
            new LambdaQueryWrapper<NotifyConfig>()
                .eq(NotifyConfig::getNotifyStatus, StatusEnum.YES)
                .eq(NotifyConfig::getNotifyScene, NotifySceneEnum.RETRY_TASK_ENTER_DEAD_LETTER.getNotifyScene())
                .in(NotifyConfig::getNamespaceId, namespaceIds)
                .in(NotifyConfig::getGroupName, groupNames)
                .in(NotifyConfig::getSceneName, sceneNames)
        );

        if (CollectionUtils.isEmpty(notifyConfigs)) {
            return null;
        }

        return notifyConfigs.stream()
            .collect(Collectors.groupingBy(i -> {

                String namespaceId = i.getNamespaceId();
                String groupName = i.getGroupName();
                String sceneName = i.getSceneName();

                return ImmutableTriple.of(namespaceId, groupName, sceneName);
            }));
    }

    protected void doSendAlarm(Triple<String, String, String> key,
        List<NotifyConfig> notifyConfigsList,
        AlarmDTO alarmDTO
    ) {
        for (final NotifyConfig notifyConfig : notifyConfigsList) {
            if (Objects.equals(notifyConfig.getRateLimiterStatus(), StatusEnum.YES.getStatus())) {
                // 限流
                RateLimiter rateLimiter = getRateLimiter(String.valueOf(notifyConfig.getId()),
                    notifyConfig.getRateLimiterThreshold());
                // 每秒发送rateLimiterThreshold个告警
                if (Objects.nonNull(rateLimiter) && !rateLimiter.tryAcquire(1, TimeUnit.SECONDS)) {
                    LogUtils.warn(log,
                        "namespaceId:[{}] groupName:[{}] senceName:[{}] idempotentId:[{}]  任务重试失败进入死信队列已到达最大限流阈值,本次通知不执行",
                        key.getLeft(), key.getMiddle(), key.getRight(),
                        alarmDTO.getIdempotentId());
                    continue;
                }
            }

            AlarmContext context = buildAlarmContext(alarmDTO, notifyConfig);
            Alarm<AlarmContext> alarmType = easyRetryAlarmFactory.getAlarmType(
                notifyConfig.getNotifyType());
            alarmType.asyncSendMessage(context);
        }
    }

    protected abstract AlarmContext buildAlarmContext(final AlarmDTO alarmDTO, NotifyConfig notifyConfig);

    @Data
    public static class AlarmDTO {

        private String uniqueId;

        private String groupName;

        private String sceneName;

        private String idempotentId;

        private String bizNo;

        private String executorName;

        private String argsStr;

        private Integer retryCount;

        private LocalDateTime createDt;
    }

    @Mapper
    public interface AlarmDTOConverter {

        AlarmDTOConverter INSTANCE = Mappers.getMapper(AlarmDTOConverter.class);

        AlarmDTO toAlarmDTO(RetryDeadLetter retryDeadLetter);

        AlarmDTO toAlarmDTO(RetryTask retryTask);
    }
}
