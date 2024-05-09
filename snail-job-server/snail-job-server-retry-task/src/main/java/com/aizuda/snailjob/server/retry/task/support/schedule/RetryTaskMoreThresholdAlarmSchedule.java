package com.aizuda.snailjob.server.retry.task.support.schedule;

import com.aizuda.snailjob.common.core.alarm.AlarmContext;
import com.aizuda.snailjob.common.core.alarm.SnailJobAlarmFactory;
import com.aizuda.snailjob.common.core.enums.RetryNotifySceneEnum;
import com.aizuda.snailjob.common.core.enums.RetryStatusEnum;
import com.aizuda.snailjob.common.core.enums.StatusEnum;
import com.aizuda.snailjob.common.core.util.EnvironmentUtils;
import com.aizuda.snailjob.common.core.util.JsonUtil;
import com.aizuda.snailjob.common.core.util.StreamUtils;
import com.aizuda.snailjob.server.common.Lifecycle;
import com.aizuda.snailjob.server.common.dto.PartitionTask;
import com.aizuda.snailjob.server.common.enums.SyetemTaskTypeEnum;
import com.aizuda.snailjob.server.common.schedule.AbstractSchedule;
import com.aizuda.snailjob.server.common.util.DateUtils;
import com.aizuda.snailjob.server.common.util.PartitionTaskUtils;
import com.aizuda.snailjob.server.retry.task.dto.NotifyConfigPartitionTask;
import com.aizuda.snailjob.server.retry.task.dto.NotifyConfigPartitionTask.RecipientInfo;
import com.aizuda.snailjob.server.retry.task.support.RetryTaskConverter;
import com.aizuda.snailjob.template.datasource.access.AccessTemplate;
import com.aizuda.snailjob.template.datasource.persistence.mapper.NotifyRecipientMapper;
import com.aizuda.snailjob.template.datasource.persistence.po.NotifyConfig;
import com.aizuda.snailjob.template.datasource.persistence.po.NotifyRecipient;
import com.aizuda.snailjob.template.datasource.persistence.po.RetryTask;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;


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
public class RetryTaskMoreThresholdAlarmSchedule extends AbstractSchedule implements Lifecycle {

    private static final String retryTaskMoreThresholdTextMessageFormatter =
        "<font face=\"微软雅黑\" color=#ff0000 size=4>{}环境 场景重试数量超过{}个</font>  \n" +
            "> 空间ID:{}  \n" +
            "> 组名称:{}  \n" +
            "> 场景名称:{}  \n" +
            "> 告警时间:{}  \n" +
            "> **共计:{}**  \n";

    private final AccessTemplate accessTemplate;
    private final NotifyRecipientMapper recipientMapper;

    @Override
    public void start() {
        taskScheduler.scheduleWithFixedDelay(this::execute, Instant.now(), Duration.parse("PT10M"));
    }

    @Override
    public void close() {
    }

    @Override
    protected void doExecute() {
        PartitionTaskUtils.process(this::getNotifyConfigPartitions, this::doHandler, 0);
    }

    private void doHandler(List<? extends PartitionTask> partitionTasks) {
        for (PartitionTask partitionTask : partitionTasks) {
            doSendAlarm((NotifyConfigPartitionTask) partitionTask);
        }
    }

    private void doSendAlarm(NotifyConfigPartitionTask partitionTask) {
        long count = accessTemplate.getRetryTaskAccess()
            .count(partitionTask.getGroupName(), partitionTask.getNamespaceId(),
                new LambdaQueryWrapper<RetryTask>()
                    .eq(RetryTask::getNamespaceId, partitionTask.getNamespaceId())
                    .eq(RetryTask::getGroupName, partitionTask.getGroupName())
                    .eq(RetryTask::getSceneName, partitionTask.getBusinessId())
                    .eq(RetryTask::getRetryStatus, RetryStatusEnum.RUNNING.getStatus()));
        if (count >= partitionTask.getNotifyThreshold()) {

            List<RecipientInfo> recipientInfos = partitionTask.getRecipientInfos();
            for (final RecipientInfo recipientInfo : recipientInfos) {
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
                        partitionTask.getBusinessId(),
                        DateUtils.toNowFormat(DateUtils.NORM_DATETIME_PATTERN),
                        count)
                    .title("{}环境 场景重试数量超过阈值", EnvironmentUtils.getActiveProfile())
                    .notifyAttribute(recipientInfo.getNotifyAttribute());
                Optional.ofNullable(SnailJobAlarmFactory.getAlarmType(recipientInfo.getNotifyType()))
                    .ifPresent(alarmType -> alarmType.asyncSendMessage(context));

            }
        }
    }

    private List<NotifyConfigPartitionTask> getNotifyConfigPartitions(Long startId) {

        List<NotifyConfig> notifyConfigs = accessTemplate.getNotifyConfigAccess()
            .listPage(new PageDTO<>(startId, 1000), new LambdaQueryWrapper<NotifyConfig>()
                .eq(NotifyConfig::getNotifyStatus, StatusEnum.YES.getStatus())
                .eq(NotifyConfig::getSystemTaskType, SyetemTaskTypeEnum.RETRY.getType())
                .eq(NotifyConfig::getNotifyScene, RetryNotifySceneEnum.MAX_RETRY.getNotifyScene())
                .orderByDesc(NotifyConfig::getId)) // SQLServer 分页必须 ORDER BY
            .getRecords();

        Set<Long> recipientIds = notifyConfigs.stream()
            .flatMap(config -> JsonUtil.parseList(config.getRecipientIds(), Long.class).stream())
            .collect(Collectors.toSet());

        if (CollectionUtils.isEmpty(recipientIds)) {
            return Lists.newArrayList();
        }

        Map<Long, NotifyRecipient> recipientMap = StreamUtils.toIdentityMap(
            recipientMapper.selectBatchIds(recipientIds), NotifyRecipient::getId);

        List<NotifyConfigPartitionTask> notifyConfigPartitionTasks = RetryTaskConverter.INSTANCE.toNotifyConfigPartitionTask(
            notifyConfigs);
        notifyConfigPartitionTasks.forEach(task -> {
            List<RecipientInfo> recipientList = StreamUtils.toList(task.getRecipientIds(), recipientId -> {
                NotifyRecipient notifyRecipient = recipientMap.get(recipientId);
                if (Objects.isNull(notifyRecipient)) {
                    return null;
                }

                RecipientInfo recipientInfo = new RecipientInfo();
                recipientInfo.setNotifyType(notifyRecipient.getNotifyType());
                recipientInfo.setNotifyAttribute(notifyRecipient.getNotifyAttribute());

                return recipientInfo;
            });
            task.setRecipientInfos(recipientList);
        });

        return notifyConfigPartitionTasks;
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
