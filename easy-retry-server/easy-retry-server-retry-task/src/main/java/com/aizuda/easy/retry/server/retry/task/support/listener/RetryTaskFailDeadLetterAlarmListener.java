package com.aizuda.easy.retry.server.retry.task.support.listener;

import com.aizuda.easy.retry.common.core.alarm.AlarmContext;
import com.aizuda.easy.retry.common.core.enums.NotifySceneEnum;
import com.aizuda.easy.retry.common.core.log.LogUtils;
import com.aizuda.easy.retry.common.core.util.EnvironmentUtils;
import com.aizuda.easy.retry.common.core.util.HostUtils;
import com.aizuda.easy.retry.server.common.AlarmInfoConverter;
import com.aizuda.easy.retry.server.common.Lifecycle;
import com.aizuda.easy.retry.server.common.alarm.AbstractAlarm;
import com.aizuda.easy.retry.server.common.alarm.AbstractFlowControl;
import com.aizuda.easy.retry.server.common.alarm.AbstractRetryAlarm;
import com.aizuda.easy.retry.server.common.dto.AlarmInfo;
import com.aizuda.easy.retry.server.common.dto.NotifyConfigInfo;
import com.aizuda.easy.retry.server.common.dto.RetryAlarmInfo;
import com.aizuda.easy.retry.server.common.util.DateUtils;
import com.aizuda.easy.retry.server.common.triple.ImmutableTriple;
import com.aizuda.easy.retry.server.common.triple.Triple;
import com.aizuda.easy.retry.server.retry.task.support.event.RetryTaskFailDeadLetterAlarmEvent;
import com.aizuda.easy.retry.template.datasource.access.AccessTemplate;
import com.aizuda.easy.retry.template.datasource.persistence.po.NotifyConfig;
import com.aizuda.easy.retry.template.datasource.persistence.po.RetryDeadLetter;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 重试任务失败进入死信队列监听器
 *
 * @author: zuoJunLin
 * @date : 2023-11-20 21:40
 * @since 2.5.0
 */
@Component
@Slf4j
public class RetryTaskFailDeadLetterAlarmListener extends AbstractRetryAlarm<RetryTaskFailDeadLetterAlarmEvent> implements Runnable, Lifecycle {

    /**
     * 死信告警数据
     */
    private LinkedBlockingQueue<List<RetryDeadLetter>> queue = new LinkedBlockingQueue<>(1000);

    private static String retryTaskDeadTextMessagesFormatter =
            "<font face=\"微软雅黑\" color=#ff0000 size=4>{}环境 重试任务失败进入死信队列</font>  \n" +
                    "> 组名称:{}  \n" +
                    "> 执行器名称:{}  \n" +
                    "> 场景名称:{}  \n" +
                    "> 业务数据:{}  \n" +
                    "> 时间:{}  \n";

    @Override
    protected List<RetryAlarmInfo> poll() throws InterruptedException {

        List<RetryDeadLetter> allRetryDeadLetterList = queue.poll(5, TimeUnit.SECONDS);
        if (CollectionUtils.isEmpty(allRetryDeadLetterList)) {
            return Lists.newArrayList();
        }

        return AlarmInfoConverter.INSTANCE.deadLetterToAlarmInfo(allRetryDeadLetterList);
    }

    @Override
    public void onApplicationEvent(RetryTaskFailDeadLetterAlarmEvent event) {
        if (!queue.offer(event.getRetryDeadLetters())) {
            LogUtils.warn(log, "任务重试失败进入死信队列告警队列已满");
        }
    }

    @Override
    protected AlarmContext buildAlarmContext(final RetryAlarmInfo retryAlarmInfo, final NotifyConfigInfo notifyConfig) {

        // 预警
        return AlarmContext.build().text(retryTaskDeadTextMessagesFormatter,
                        EnvironmentUtils.getActiveProfile(),
                        retryAlarmInfo.getGroupName(),
                        retryAlarmInfo.getExecutorName(),
                        retryAlarmInfo.getSceneName(),
                        retryAlarmInfo.getArgsStr(),
                        DateUtils.format(retryAlarmInfo.getCreateDt(), DateUtils.NORM_DATETIME_PATTERN))
                .title("组:[{}] 场景:[{}] 环境重试任务失败进入死信队列",
                        retryAlarmInfo.getGroupName(), retryAlarmInfo.getSceneName())
                .notifyAttribute(notifyConfig.getNotifyAttribute());
    }

    @Override
    protected void startLog() {
        LogUtils.info(log, "RetryTaskFailDeadLetterAlarmListener started");
    }

    @Override
    protected int getNotifyScene() {
        return NotifySceneEnum.MAX_RETRY.getNotifyScene();
    }
}
