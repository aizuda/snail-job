package com.aizuda.easy.retry.server.support.schedule;

import com.aizuda.easy.retry.common.core.alarm.Alarm;
import com.aizuda.easy.retry.common.core.alarm.AlarmContext;
import com.aizuda.easy.retry.common.core.alarm.EasyRetryAlarmFactory;
import com.aizuda.easy.retry.common.core.enums.NotifySceneEnum;
import com.aizuda.easy.retry.common.core.enums.RetryStatusEnum;
import com.aizuda.easy.retry.common.core.log.LogUtils;
import com.aizuda.easy.retry.common.core.util.EnvironmentUtils;
import com.aizuda.easy.retry.common.core.util.HostUtils;
import com.aizuda.easy.retry.server.persistence.mybatis.mapper.RetryDeadLetterMapper;
import com.aizuda.easy.retry.server.persistence.mybatis.mapper.RetryTaskMapper;
import com.aizuda.easy.retry.server.persistence.mybatis.po.GroupConfig;
import com.aizuda.easy.retry.server.persistence.mybatis.po.NotifyConfig;
import com.aizuda.easy.retry.server.persistence.support.ConfigAccess;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * @author: www.byteblogs.com
 * @date : 2021-11-24 14:58
 */
@Component
@Slf4j
@Deprecated
public class AlarmNotifyThreadSchedule {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static String retryErrorMoreThresholdTextMessageFormatter =
            "<font face=\"微软雅黑\" color=#ff0000 size=4>{}环境 重试失败数据监控</font>  \n" +
                    "> 名称:{}  \n" +
                    "> 时间窗口:{} ~ {}  \n" +
                    "> **共计:{}**  \n";

    private static String retryTaskMoreThresholdTextMessageFormatter =
            "<font face=\"微软雅黑\" color=#ff0000 size=4>{}环境 重试数据监控</font>  \n" +
                    "> 名称:{}  \n" +
                    "> 时间:{}  \n" +
                    "> **共计:{}**  \n";

    @Autowired
    private RetryDeadLetterMapper retryDeadLetterMapper;
    @Autowired
    private RetryTaskMapper retryTaskMapper;
    @Autowired
    private EasyRetryAlarmFactory easyRetryAlarmFactory;
    @Autowired
    @Qualifier("configAccessProcessor")
    private ConfigAccess configAccess;

    /**
     * 监控重试表中数据总量是否到达阈值
     */
//    @Scheduled(cron = "0 0/10 * * * ?")
//    @SchedulerLock(name = "retryTaskMoreThreshold", lockAtMostFor = "PT10M", lockAtLeastFor = "PT10M")
    public void retryTaskMoreThreshold() {
        LogUtils.info(log, "retryTaskMoreThreshold time[{}] ip:[{}]", LocalDateTime.now(), HostUtils.getIp());

        for (GroupConfig groupConfig : configAccess.getAllConfigGroupList()) {
            List<NotifyConfig> notifyConfigs = configAccess.getNotifyConfigByGroupName(groupConfig.getGroupName(), NotifySceneEnum.MAX_RETRY.getNotifyScene());
            if (CollectionUtils.isEmpty(notifyConfigs)) {
                continue;
            }

            int count = retryTaskMapper.countAllRetryTaskByRetryStatus(groupConfig.getGroupPartition(), RetryStatusEnum.RUNNING.getStatus());
            for (NotifyConfig notifyConfig : notifyConfigs) {
                if (count > notifyConfig.getNotifyThreshold()) {
                    // 预警
                    AlarmContext context = AlarmContext.build()
                            .text(retryTaskMoreThresholdTextMessageFormatter,
                                    EnvironmentUtils.getActiveProfile(),
                                    groupConfig.getGroupName(),
                                    LocalDateTime.now().format(formatter),
                                    count)
                            .title("组:[{}])重试数据过多", groupConfig.getGroupName())
                            .notifyAttribute(notifyConfig.getNotifyAttribute());

                    Alarm<AlarmContext> alarmType = easyRetryAlarmFactory.getAlarmType(notifyConfig.getNotifyType());
                    alarmType.asyncSendMessage(context);
                }
            }
        }
    }

    /**
     * 监控重试失败数据总量是否到达阈值
     */
//    @Scheduled(cron = "0 0/11 * * * ?")
//    @SchedulerLock(name = "retryErrorMoreThreshold", lockAtMostFor = "PT11M", lockAtLeastFor = "PT11M")
    public void retryErrorMoreThreshold() {
        LogUtils.info(log, "retryErrorMoreThreshold time[{}] ip:[{}]", LocalDateTime.now(), HostUtils.getIp());

        for (GroupConfig groupConfig : configAccess.getAllConfigGroupList()) {
            List<NotifyConfig> notifyConfigs = configAccess.getNotifyConfigByGroupName(groupConfig.getGroupName(), NotifySceneEnum.MAX_RETRY_ERROR.getNotifyScene());
            if (CollectionUtils.isEmpty(notifyConfigs)) {
                continue;
            }

            // x分钟内进入死信队列的数据量
            LocalDateTime now = LocalDateTime.now();
            int count = retryDeadLetterMapper.countRetryDeadLetterByCreateAt(now.minusMinutes(30), now, groupConfig.getGroupPartition());

            for (NotifyConfig notifyConfig : notifyConfigs) {
                if (count > notifyConfig.getNotifyThreshold()) {
                    // 预警
                    AlarmContext context = AlarmContext.build()
                            .text(retryErrorMoreThresholdTextMessageFormatter,
                                    EnvironmentUtils.getActiveProfile(),
                                    groupConfig.getGroupName(),
                                    now.minusMinutes(30).format(formatter),
                                    now.format(formatter),
                                    count)
                            .title("组:[{}] 环境重试失败数据监控", groupConfig.getGroupName())
                            .notifyAttribute(notifyConfig.getNotifyAttribute());

                    Alarm<AlarmContext> alarmType = easyRetryAlarmFactory.getAlarmType(notifyConfig.getNotifyType());
                    alarmType.asyncSendMessage(context);
                }
            }
        }
    }
}
