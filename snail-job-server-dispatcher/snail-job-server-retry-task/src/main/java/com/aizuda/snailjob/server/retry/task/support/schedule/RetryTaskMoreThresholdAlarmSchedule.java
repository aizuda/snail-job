package com.aizuda.snailjob.server.retry.task.support.schedule;

import com.aizuda.snailjob.common.core.alarm.AlarmContext;
import com.aizuda.snailjob.common.core.alarm.SnailJobAlarmFactory;
import com.aizuda.snailjob.common.core.enums.RetryNotifySceneEnum;
import com.aizuda.snailjob.common.core.enums.RetryStatusEnum;
import com.aizuda.snailjob.common.core.util.EnvironmentUtils;
import com.aizuda.snailjob.server.common.Lifecycle;
import com.aizuda.snailjob.server.common.util.DateUtils;
import com.aizuda.snailjob.server.retry.task.dto.NotifyConfigDTO;
import com.aizuda.snailjob.server.retry.task.dto.RetrySceneConfigPartitionTask;
import com.aizuda.snailjob.template.datasource.persistence.po.Retry;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.*;


/**
 * 监控重试表中数据总量是否到达阈值
 *
 * @author: opensnail
 * @date : 2023-07-21 17:25
 * @since 2.1.0
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class RetryTaskMoreThresholdAlarmSchedule extends AbstractRetryTaskAlarmSchedule implements Lifecycle {

    private static final String retryTaskMoreThresholdTextMessageFormatter =
            "<font face=\"微软雅黑\" color=#ff0000 size=4>{}环境 场景重试数量超过{}个</font>  \n" +
                    "> 空间ID:{}  \n" +
                    "> 组名称:{}  \n" +
                    "> 场景名称:{}  \n" +
                    "> 告警时间:{}  \n" +
                    "> **共计:{}**  \n";

    @Override
    public void start() {
        taskScheduler.scheduleWithFixedDelay(this::execute, Instant.now(), Duration.parse("PT10M"));
    }

    @Override
    public void close() {
    }

    @Override
    protected void doSendAlarm(RetrySceneConfigPartitionTask partitionTask, Map<Long, NotifyConfigDTO> notifyConfigInfo) {

        // x分钟内、x组、x场景进入重试任务的数据量
        long count = accessTemplate.getRetryAccess()
                .count(new LambdaQueryWrapper<Retry>()
                                .eq(Retry::getNamespaceId, partitionTask.getNamespaceId())
                                .eq(Retry::getGroupName, partitionTask.getGroupName())
                                .eq(Retry::getSceneName, partitionTask.getSceneName())
                                .eq(Retry::getRetryStatus, RetryStatusEnum.RUNNING.getStatus()));
        for (Long notifyId : partitionTask.getNotifyIds()) {
            NotifyConfigDTO notifyConfigDTO = notifyConfigInfo.get(notifyId);
            if (notifyConfigDTO == null) {
                continue;
            }

            if (notifyConfigDTO.getNotifyThreshold() > 0 && count >= notifyConfigDTO.getNotifyThreshold()) {
                List<NotifyConfigDTO.RecipientInfo> recipientInfos = notifyConfigDTO.getRecipientInfos();
                for (final NotifyConfigDTO.RecipientInfo recipientInfo : recipientInfos) {
                    if (Objects.isNull(recipientInfo)) {
                        continue;
                    }
                    // 预警
                    AlarmContext context = AlarmContext.build()
                            .text(retryTaskMoreThresholdTextMessageFormatter,
                                    EnvironmentUtils.getActiveProfile(),
                                    count,
                                    partitionTask.getNamespaceId(),
                                    partitionTask.getGroupName(),
                                    partitionTask.getSceneName(),
                                    DateUtils.toNowFormat(DateUtils.NORM_DATETIME_PATTERN),
                                    count)
                            .title("{} environment scenario retry count exceeds threshold", EnvironmentUtils.getActiveProfile())
                            .notifyAttribute(recipientInfo.getNotifyAttribute());
                    Optional.ofNullable(SnailJobAlarmFactory.getAlarmType(recipientInfo.getNotifyType()))
                            .ifPresent(alarmType -> alarmType.asyncSendMessage(context));

                }
            }
        }

    }

    @Override
    protected RetryNotifySceneEnum getNotifyScene() {
        return RetryNotifySceneEnum.MAX_RETRY;
    }

    @Override
    public String lockName() {
        return "retryTaskMoreThreshold";
    }

    @Override
    public String lockAtMost() {
        return "PT10M";
    }

    @Override
    public String lockAtLeast() {
        return "PT1M";
    }
}
