package com.aizuda.easy.retry.server.retry.task.support.schedule;

import com.aizuda.easy.retry.common.core.alarm.Alarm;
import com.aizuda.easy.retry.common.core.alarm.AlarmContext;
import com.aizuda.easy.retry.common.core.alarm.EasyRetryAlarmFactory;
import com.aizuda.easy.retry.common.core.constant.SystemConstants.DATE_FORMAT;
import com.aizuda.easy.retry.common.core.enums.NotifySceneEnum;
import com.aizuda.easy.retry.common.core.enums.RetryStatusEnum;
import com.aizuda.easy.retry.common.core.log.LogUtils;
import com.aizuda.easy.retry.common.core.util.EnvironmentUtils;
import com.aizuda.easy.retry.common.core.util.HostUtils;
import com.aizuda.easy.retry.server.common.Lifecycle;
import com.aizuda.easy.retry.template.datasource.access.AccessTemplate;
import com.aizuda.easy.retry.template.datasource.persistence.po.GroupConfig;
import com.aizuda.easy.retry.template.datasource.persistence.po.NotifyConfig;
import com.aizuda.easy.retry.template.datasource.persistence.po.RetryTask;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

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
        "<font face=\"微软雅黑\" color=#ff0000 size=4>{}环境 重试数据监控</font>  \n" +
            "> 组名称:{}  \n" +
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
        LogUtils.info(log, "retryTaskMoreThreshold time[{}] ip:[{}]", LocalDateTime.now(), HostUtils.getIp());

        for (GroupConfig groupConfig : accessTemplate.getGroupConfigAccess().getAllConfigGroupList()) {
            List<NotifyConfig> notifyConfigs = accessTemplate.getNotifyConfigAccess().getNotifyConfigByGroupName(groupConfig.getGroupName(), NotifySceneEnum.MAX_RETRY.getNotifyScene());
            if (CollectionUtils.isEmpty(notifyConfigs)) {
                continue;
            }

            long count = accessTemplate.getRetryTaskAccess().count(groupConfig.getGroupName(), new LambdaQueryWrapper<RetryTask>()
                    .eq(RetryTask::getGroupName, groupConfig.getGroupName())
                    .eq(RetryTask::getRetryStatus, RetryStatusEnum.RUNNING.getStatus()));
            for (NotifyConfig notifyConfig : notifyConfigs) {
                if (count > notifyConfig.getNotifyThreshold()) {
                    // 预警
                    AlarmContext context = AlarmContext.build()
                        .text(retryTaskMoreThresholdTextMessageFormatter,
                            EnvironmentUtils.getActiveProfile(),
                            groupConfig.getGroupName(),
                            LocalDateTime.now().format(DATE_FORMAT.YYYYMMDDHHMMSS),
                            count)
                        .title("组:[{}])重试数据过多", groupConfig.getGroupName())
                        .notifyAttribute(notifyConfig.getNotifyAttribute());

                    Alarm<AlarmContext> alarmType = easyRetryAlarmFactory.getAlarmType(notifyConfig.getNotifyType());
                    alarmType.asyncSendMessage(context);
                }
            }
        }
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
