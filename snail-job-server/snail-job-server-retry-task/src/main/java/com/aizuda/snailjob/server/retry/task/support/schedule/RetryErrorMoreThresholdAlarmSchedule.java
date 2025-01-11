package com.aizuda.snailjob.server.retry.task.support.schedule;

import cn.hutool.core.collection.CollUtil;
import com.aizuda.snailjob.common.core.alarm.Alarm;
import com.aizuda.snailjob.common.core.alarm.AlarmContext;
import com.aizuda.snailjob.common.core.alarm.SnailJobAlarmFactory;
import com.aizuda.snailjob.common.core.enums.RetryNotifySceneEnum;
import com.aizuda.snailjob.common.core.util.EnvironmentUtils;
import com.aizuda.snailjob.server.common.Lifecycle;
import com.aizuda.snailjob.server.common.util.DateUtils;
import com.aizuda.snailjob.server.retry.task.dto.NotifyConfigDTO;
import com.aizuda.snailjob.server.retry.task.dto.RetrySceneConfigPartitionTask;
import com.aizuda.snailjob.template.datasource.access.TaskAccess;
import com.aizuda.snailjob.template.datasource.persistence.po.RetryDeadLetter;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 监控重试失败数据总量是否到达阈值
 *
 * @author: opensnail
 * @date : 2023-07-21 17:25
 * @since 2.1.0
 */
@Component
@RequiredArgsConstructor
public class RetryErrorMoreThresholdAlarmSchedule extends AbstractRetryTaskAlarmSchedule implements Lifecycle {

    private static final String retryErrorMoreThresholdTextMessageFormatter =
            "<font face=\"微软雅黑\" color=#ff0000 size=4>{}环境 场景重试失败数量超过{}个</font>  \n" +
                    "> 空间ID:{}  \n" +
                    "> 组名称:{}  \n" +
                    "> 场景名称:{}  \n" +
                    "> 时间窗口:{} ~ {}  \n" +
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
        if (CollUtil.isEmpty(partitionTask.getNotifyIds())) {
            return;
        }

        // x分钟内、x组、x场景进入死信队列的数据量
        LocalDateTime now = LocalDateTime.now();
        TaskAccess<RetryDeadLetter> retryDeadLetterAccess = accessTemplate.getRetryDeadLetterAccess();
        long count = retryDeadLetterAccess.count(partitionTask.getGroupName(),
                partitionTask.getNamespaceId(),
                new LambdaQueryWrapper<RetryDeadLetter>().
                        between(RetryDeadLetter::getCreateDt, now.minusMinutes(30), now)
                        .eq(RetryDeadLetter::getNamespaceId, partitionTask.getNamespaceId())
                        .eq(RetryDeadLetter::getGroupName, partitionTask.getGroupName())
                        .eq(RetryDeadLetter::getSceneName, partitionTask.getSceneName()));

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
                            .text(retryErrorMoreThresholdTextMessageFormatter,
                                    EnvironmentUtils.getActiveProfile(),
                                    count,
                                    partitionTask.getNamespaceId(),
                                    partitionTask.getGroupName(),
                                    partitionTask.getSceneName(),
                                    DateUtils.format(now.minusMinutes(30),
                                            DateUtils.NORM_DATETIME_PATTERN),
                                    DateUtils.toNowFormat(DateUtils.NORM_DATETIME_PATTERN), count)
                            .title("{}环境 场景重试失败数量超过阈值", EnvironmentUtils.getActiveProfile())
                            .notifyAttribute(recipientInfo.getNotifyAttribute());
                    Alarm<AlarmContext> alarmType = SnailJobAlarmFactory.getAlarmType(
                            recipientInfo.getNotifyType());
                    alarmType.asyncSendMessage(context);
                }

            }
        }
    }

    @Override
    protected RetryNotifySceneEnum getNotifyScene() {
        return RetryNotifySceneEnum.MAX_RETRY_ERROR;
    }


    @Override
    public String lockName() {
        return "retryErrorMoreThreshold";
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
