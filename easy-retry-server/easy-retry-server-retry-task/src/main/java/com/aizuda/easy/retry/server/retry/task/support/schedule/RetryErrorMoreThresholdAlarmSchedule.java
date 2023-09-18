package com.aizuda.easy.retry.server.retry.task.support.schedule;

import com.aizuda.easy.retry.common.core.alarm.Alarm;
import com.aizuda.easy.retry.common.core.alarm.AlarmContext;
import com.aizuda.easy.retry.common.core.alarm.EasyRetryAlarmFactory;
import com.aizuda.easy.retry.common.core.constant.SystemConstants.DATE_FORMAT;
import com.aizuda.easy.retry.common.core.enums.NotifySceneEnum;
import com.aizuda.easy.retry.common.core.log.LogUtils;
import com.aizuda.easy.retry.common.core.util.EnvironmentUtils;
import com.aizuda.easy.retry.common.core.util.HostUtils;
import com.aizuda.easy.retry.server.common.Lifecycle;
import com.aizuda.easy.retry.template.datasource.access.AccessTemplate;
import com.aizuda.easy.retry.template.datasource.access.TaskAccess;
import com.aizuda.easy.retry.template.datasource.persistence.po.GroupConfig;
import com.aizuda.easy.retry.template.datasource.persistence.po.NotifyConfig;
import com.aizuda.easy.retry.template.datasource.persistence.po.RetryDeadLetter;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 监控重试失败数据总量是否到达阈值
 *
 * @author: www.byteblogs.com
 * @date : 2023-07-21 17:25
 * @since 2.1.0
 */
@Component
@Slf4j
public class RetryErrorMoreThresholdAlarmSchedule extends AbstractSchedule implements Lifecycle {
    private static String retryErrorMoreThresholdTextMessageFormatter =
        "<font face=\"微软雅黑\" color=#ff0000 size=4>{}环境 重试失败数据监控</font>  \n" +
            "> 名称:{}  \n" +
            "> 时间窗口:{} ~ {}  \n" +
            "> **共计:{}**  \n";

    @Autowired
    private EasyRetryAlarmFactory easyRetryAlarmFactory;
    @Autowired
    protected AccessTemplate accessTemplate;

    @Override
    public void start() {
        taskScheduler.scheduleWithFixedDelay(this::execute, Instant.now(), Duration.parse("PT10M"));
    }

    @Override
    public void close() {

    }

    @Override
    protected void doExecute() {
        LogUtils.info(log, "retryErrorMoreThreshold time[{}] ip:[{}]", LocalDateTime.now(), HostUtils.getIp());

        for (GroupConfig groupConfig : accessTemplate.getGroupConfigAccess().getAllConfigGroupList()) {
            List<NotifyConfig> notifyConfigs = accessTemplate.getNotifyConfigAccess().getNotifyConfigByGroupName(groupConfig.getGroupName(), NotifySceneEnum.MAX_RETRY_ERROR.getNotifyScene());
            if (CollectionUtils.isEmpty(notifyConfigs)) {
                continue;
            }

            // x分钟内进入死信队列的数据量
            LocalDateTime now = LocalDateTime.now();
            TaskAccess<RetryDeadLetter> retryDeadLetterAccess = accessTemplate.getRetryDeadLetterAccess();

            long count = retryDeadLetterAccess.count(groupConfig.getGroupName(), new LambdaQueryWrapper<RetryDeadLetter>()
                    .between(RetryDeadLetter::getCreateDt, now.minusMinutes(30), now));
            for (NotifyConfig notifyConfig : notifyConfigs) {
                if (count > notifyConfig.getNotifyThreshold()) {
                    // 预警
                    AlarmContext context = AlarmContext.build()
                        .text(retryErrorMoreThresholdTextMessageFormatter,
                            EnvironmentUtils.getActiveProfile(),
                            groupConfig.getGroupName(),
                            now.minusMinutes(30).format(DATE_FORMAT.YYYYMMDDHHMMSS),
                            now.format(DATE_FORMAT.YYYYMMDDHHMMSS),
                            count)
                        .title("组:[{}] 环境重试失败数据监控", groupConfig.getGroupName())
                        .notifyAttribute(notifyConfig.getNotifyAttribute());

                    Alarm<AlarmContext> alarmType = easyRetryAlarmFactory.getAlarmType(notifyConfig.getNotifyType());
                    alarmType.asyncSendMessage(context);
                }
            }
        }
    }

    @Override
    String lockName() {
        return "retryErrorMoreThreshold";
    }

    @Override
    String lockAtMost() {
        return "PT10M";
    }

    @Override
    String lockAtLeast() {
        return "PT1M";
    }
}
