package com.aizuda.snailjob.server.retry.task.support.listener;

import cn.hutool.core.collection.CollUtil;
import com.aizuda.snailjob.common.core.alarm.AlarmContext;
import com.aizuda.snailjob.common.core.enums.RetryNotifySceneEnum;
import com.aizuda.snailjob.common.core.util.EnvironmentUtils;
import com.aizuda.snailjob.common.log.SnailJobLog;
import com.aizuda.snailjob.server.common.Lifecycle;
import com.aizuda.snailjob.server.common.alarm.AbstractRetryAlarm;
import com.aizuda.snailjob.server.common.convert.AlarmInfoConverter;
import com.aizuda.snailjob.server.common.dto.NotifyConfigInfo;
import com.aizuda.snailjob.server.common.dto.RetryAlarmInfo;
import com.aizuda.snailjob.server.common.enums.SyetemTaskTypeEnum;
import com.aizuda.snailjob.server.common.util.DateUtils;
import com.aizuda.snailjob.server.retry.task.dto.RetryTaskFailDeadLetterAlarmEventDTO;
import com.aizuda.snailjob.server.retry.task.support.RetryTaskConverter;
import com.aizuda.snailjob.server.retry.task.support.event.RetryTaskFailDeadLetterAlarmEvent;
import com.aizuda.snailjob.template.datasource.persistence.po.RetryDeadLetter;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * 重试任务失败进入死信队列监听器
 *
 * @author: zuoJunLin
 * @date : 2023-11-20 21:40
 * @since 2.5.0
 */
@Component
@Slf4j
public class RetryTaskFailDeadLetterAlarmListener extends
        AbstractRetryAlarm<RetryTaskFailDeadLetterAlarmEvent> implements Runnable, Lifecycle {

    /**
     * 死信告警数据
     */
    private final LinkedBlockingQueue<List<RetryTaskFailDeadLetterAlarmEventDTO>> queue = new LinkedBlockingQueue<>(1000);

    private static final String retryTaskDeadTextMessagesFormatter =
            "<font face=\"微软雅黑\" color=#ff0000 size=4>{}环境 重试任务失败进入死信队列</font>  \n" +
                    "> 空间ID:{}  \n" +
                    "> 组名称:{}  \n" +
                    "> 执行器名称:{}  \n" +
                    "> 场景名称:{}  \n" +
                    "> 业务数据:{}  \n" +
                    "> 时间:{}  \n";

    @Override
    protected List<SyetemTaskTypeEnum> getSystemTaskType() {
        return Lists.newArrayList(SyetemTaskTypeEnum.RETRY);
    }

    @Override
    protected List<RetryAlarmInfo> poll() throws InterruptedException {
        // 无数据时阻塞线程
        List<RetryTaskFailDeadLetterAlarmEventDTO> allRetryDeadLetterList = queue.poll(100, TimeUnit.MILLISECONDS);
        if (CollUtil.isEmpty(allRetryDeadLetterList)) {
            return Lists.newArrayList();
        }

        return RetryTaskConverter.INSTANCE.toRetryAlarmInfos(allRetryDeadLetterList);
    }

    @Override
    @TransactionalEventListener(fallbackExecution = true, phase = TransactionPhase.AFTER_COMPLETION)
    public void doOnApplicationEvent(RetryTaskFailDeadLetterAlarmEvent event) {
        if (!queue.offer(event.getRetryDeadLetters())) {
            SnailJobLog.LOCAL.warn("任务重试失败进入死信队列告警队列已满");
        }
    }

    @Override
    protected AlarmContext buildAlarmContext(final RetryAlarmInfo retryAlarmInfo, final NotifyConfigInfo notifyConfig) {

        // 预警
        return AlarmContext.build().text(retryTaskDeadTextMessagesFormatter,
                        EnvironmentUtils.getActiveProfile(),
                        retryAlarmInfo.getNamespaceId(),
                        retryAlarmInfo.getGroupName(),
                        retryAlarmInfo.getExecutorName(),
                        retryAlarmInfo.getSceneName(),
                        retryAlarmInfo.getArgsStr(),
                        DateUtils.toNowFormat(DateUtils.NORM_DATETIME_PATTERN))
                .title("组:[{}] 场景:[{}] 环境重试任务失败进入死信队列",
                        retryAlarmInfo.getGroupName(), retryAlarmInfo.getSceneName());
    }

    @Override
    protected void startLog() {
        SnailJobLog.LOCAL.info("RetryTaskFailDeadLetterAlarmListener started");
    }

    @Override
    protected int getNotifyScene() {
        return RetryNotifySceneEnum.RETRY_TASK_ENTER_DEAD_LETTER.getNotifyScene();
    }
}
