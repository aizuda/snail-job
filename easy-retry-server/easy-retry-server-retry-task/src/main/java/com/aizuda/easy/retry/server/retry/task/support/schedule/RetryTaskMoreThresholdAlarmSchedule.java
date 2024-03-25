package com.aizuda.easy.retry.server.retry.task.support.schedule;

import com.aizuda.easy.retry.common.core.alarm.Alarm;
import com.aizuda.easy.retry.common.core.alarm.AlarmContext;
import com.aizuda.easy.retry.common.core.alarm.EasyRetryAlarmFactory;
import com.aizuda.easy.retry.common.core.enums.NotifySceneEnum;
import com.aizuda.easy.retry.common.core.enums.RetryStatusEnum;
import com.aizuda.easy.retry.common.core.enums.StatusEnum;
import com.aizuda.easy.retry.common.log.EasyRetryLog;
import com.aizuda.easy.retry.common.core.util.EnvironmentUtils;
import com.aizuda.easy.retry.common.core.util.NetUtil;
import com.aizuda.easy.retry.server.common.Lifecycle;
import com.aizuda.easy.retry.server.common.dto.PartitionTask;
import com.aizuda.easy.retry.server.common.schedule.AbstractSchedule;
import com.aizuda.easy.retry.server.common.util.DateUtils;
import com.aizuda.easy.retry.server.common.util.PartitionTaskUtils;
import com.aizuda.easy.retry.server.retry.task.dto.NotifyConfigPartitionTask;
import com.aizuda.easy.retry.server.retry.task.support.RetryTaskConverter;
import com.aizuda.easy.retry.template.datasource.access.AccessTemplate;
import com.aizuda.easy.retry.template.datasource.persistence.po.NotifyConfig;
import com.aizuda.easy.retry.template.datasource.persistence.po.RetryTask;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 监控重试表中数据总量是否到达阈值
 *
 * @author: www.byteblogs.com
 * @date : 2023-07-21 17:25
 * @since 2.1.0
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class RetryTaskMoreThresholdAlarmSchedule extends AbstractSchedule implements Lifecycle {
    private static String retryTaskMoreThresholdTextMessageFormatter =
            "<font face=\"微软雅黑\" color=#ff0000 size=4>{}环境 场景重试数量超过{}个</font>  \n" +
                    "> 空间ID:{}  \n" +
                    "> 组名称:{}  \n" +
                    "> 场景名称:{}  \n" +
                    "> 告警时间:{}  \n" +
                    "> **共计:{}**  \n";

    private final EasyRetryAlarmFactory easyRetryAlarmFactory;
    private final AccessTemplate accessTemplate;

    @Override
    public void start() {
        taskScheduler.scheduleWithFixedDelay(this::execute, Instant.now(), Duration.parse("PT10M"));
    }

    @Override
    public void close() {

    }

    @Override
    protected void doExecute() {
       EasyRetryLog.LOCAL.info("retryTaskMoreThreshold time[{}] ip:[{}]", LocalDateTime.now(), NetUtil.getLocalIpStr());
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
                                .eq(RetryTask::getSceneName, partitionTask.getSceneName())
                                .eq(RetryTask::getRetryStatus, RetryStatusEnum.RUNNING.getStatus()));
        if (count >= partitionTask.getNotifyThreshold()) {
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
                    .title("{}环境 场景重试数量超过阈值", EnvironmentUtils.getActiveProfile())
                    .notifyAttribute(partitionTask.getNotifyAttribute());
            Alarm<AlarmContext> alarmType = easyRetryAlarmFactory.getAlarmType(partitionTask.getNotifyType());
            alarmType.asyncSendMessage(context);
        }
    }

    private List<NotifyConfigPartitionTask> getNotifyConfigPartitions(Long startId) {

        List<NotifyConfig> notifyConfigs = accessTemplate.getNotifyConfigAccess()
                .listPage(new PageDTO<>(startId, 1000), new LambdaQueryWrapper<NotifyConfig>()
                        .eq(NotifyConfig::getNotifyStatus, StatusEnum.YES.getStatus())
                        .eq(NotifyConfig::getNotifyScene, NotifySceneEnum.MAX_RETRY.getNotifyScene())
                        .orderByDesc(NotifyConfig::getId)) // SQLServer 分页必须 ORDER BY
                .getRecords();

        return RetryTaskConverter.INSTANCE.toNotifyConfigPartitionTask(notifyConfigs);
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
