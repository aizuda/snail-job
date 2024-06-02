package com.aizuda.snailjob.server.retry.task.support.schedule;

import cn.hutool.core.collection.CollUtil;
import com.aizuda.snailjob.common.core.alarm.Alarm;
import com.aizuda.snailjob.common.core.alarm.AlarmContext;
import com.aizuda.snailjob.common.core.alarm.SnailJobAlarmFactory;
import com.aizuda.snailjob.common.core.enums.RetryNotifySceneEnum;
import com.aizuda.snailjob.common.core.enums.StatusEnum;
import com.aizuda.snailjob.common.core.util.EnvironmentUtils;
import com.aizuda.snailjob.common.core.util.JsonUtil;
import com.aizuda.snailjob.common.core.util.NetUtil;
import com.aizuda.snailjob.common.core.util.StreamUtils;
import com.aizuda.snailjob.common.log.SnailJobLog;
import com.aizuda.snailjob.server.common.Lifecycle;
import com.aizuda.snailjob.server.common.dto.PartitionTask;
import com.aizuda.snailjob.server.common.schedule.AbstractSchedule;
import com.aizuda.snailjob.server.common.util.DateUtils;
import com.aizuda.snailjob.server.common.util.PartitionTaskUtils;
import com.aizuda.snailjob.server.retry.task.dto.NotifyConfigPartitionTask;
import com.aizuda.snailjob.server.retry.task.dto.NotifyConfigPartitionTask.RecipientInfo;
import com.aizuda.snailjob.server.retry.task.support.RetryTaskConverter;
import com.aizuda.snailjob.template.datasource.access.AccessTemplate;
import com.aizuda.snailjob.template.datasource.access.TaskAccess;
import com.aizuda.snailjob.template.datasource.persistence.mapper.NotifyRecipientMapper;
import com.aizuda.snailjob.template.datasource.persistence.po.NotifyConfig;
import com.aizuda.snailjob.template.datasource.persistence.po.NotifyRecipient;
import com.aizuda.snailjob.template.datasource.persistence.po.RetryDeadLetter;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
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
public class RetryErrorMoreThresholdAlarmSchedule extends AbstractSchedule implements Lifecycle {

    private static final String retryErrorMoreThresholdTextMessageFormatter =
            "<font face=\"微软雅黑\" color=#ff0000 size=4>{}环境 场景重试失败数量超过{}个</font>  \n" +
                    "> 空间ID:{}  \n" +
                    "> 组名称:{}  \n" +
                    "> 场景名称:{}  \n" +
                    "> 时间窗口:{} ~ {}  \n" +
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
        SnailJobLog.LOCAL.info("retryErrorMoreThreshold time[{}] ip:[{}]", LocalDateTime.now(),
                NetUtil.getLocalIpStr());
        PartitionTaskUtils.process(this::getNotifyConfigPartitions, this::doHandler, 0);
    }

    private void doHandler(List<? extends PartitionTask> partitionTasks) {

        for (PartitionTask partitionTask : partitionTasks) {
            doSendAlarm((NotifyConfigPartitionTask) partitionTask);
        }
    }

    private void doSendAlarm(NotifyConfigPartitionTask partitionTask) {
        // x分钟内、x组、x场景进入死信队列的数据量
        LocalDateTime now = LocalDateTime.now();
        TaskAccess<RetryDeadLetter> retryDeadLetterAccess = accessTemplate.getRetryDeadLetterAccess();
        long count = retryDeadLetterAccess.count(partitionTask.getGroupName(),
                partitionTask.getNamespaceId(),
                new LambdaQueryWrapper<RetryDeadLetter>().
                        between(RetryDeadLetter::getCreateDt, now.minusMinutes(30), now)
                        .eq(RetryDeadLetter::getGroupName, partitionTask.getGroupName())
                        .eq(RetryDeadLetter::getSceneName, partitionTask.getBusinessId()));
        if (count >= partitionTask.getNotifyThreshold()) {
            List<RecipientInfo> recipientInfos = partitionTask.getRecipientInfos();
            for (final RecipientInfo recipientInfo : recipientInfos) {
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
                                partitionTask.getBusinessId(),
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

    private List<NotifyConfigPartitionTask> getNotifyConfigPartitions(Long startId) {

        List<NotifyConfig> notifyConfigs = accessTemplate.getNotifyConfigAccess()
                .listPage(new PageDTO<>(0, 1000), new LambdaQueryWrapper<NotifyConfig>()
                        .gt(NotifyConfig::getId, startId)
                        .eq(NotifyConfig::getNotifyStatus, StatusEnum.YES.getStatus())
                        .eq(NotifyConfig::getNotifyScene, RetryNotifySceneEnum.MAX_RETRY_ERROR.getNotifyScene())
                        .orderByAsc(NotifyConfig::getId)
                ).getRecords();

        if (CollUtil.isEmpty(notifyConfigs)) {
            return Lists.newArrayList();
        }

        Set<Long> recipientIds = notifyConfigs.stream()
                .map(config -> new HashSet<>(JsonUtil.parseList(config.getRecipientIds(), Long.class)))
                .reduce((a, b) -> {
                    HashSet<Long> set = Sets.newHashSet();
                    set.addAll(a);
                    set.addAll(b);
                    return set;
                }).orElse(new HashSet<>());

        if (CollUtil.isEmpty(recipientIds)) {
            return Lists.newArrayList();
        }

        List<NotifyRecipient> notifyRecipients = recipientMapper.selectBatchIds(recipientIds);
        Map<Long, NotifyRecipient> recipientMap = StreamUtils.toIdentityMap(notifyRecipients, NotifyRecipient::getId);

        List<NotifyConfigPartitionTask> notifyConfigPartitionTasks = RetryTaskConverter.INSTANCE.toNotifyConfigPartitionTask(
                notifyConfigs);
        for (final NotifyConfigPartitionTask notifyConfigPartitionTask : notifyConfigPartitionTasks) {

            List<RecipientInfo> recipientList = StreamUtils.toList(notifyConfigPartitionTask.getRecipientIds(),
                    recipientId -> {
                        NotifyRecipient notifyRecipient = recipientMap.get(recipientId);
                        if (Objects.isNull(notifyRecipient)) {
                            return null;
                        }

                        RecipientInfo recipientInfo = new RecipientInfo();
                        recipientInfo.setNotifyType(notifyRecipient.getNotifyType());
                        recipientInfo.setNotifyAttribute(notifyRecipient.getNotifyAttribute());

                        return recipientInfo;
                    });

            notifyConfigPartitionTask.setRecipientInfos(recipientList);
        }

        return notifyConfigPartitionTasks;
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
