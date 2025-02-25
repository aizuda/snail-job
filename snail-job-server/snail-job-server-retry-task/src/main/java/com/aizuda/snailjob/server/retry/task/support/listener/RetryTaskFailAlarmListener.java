package com.aizuda.snailjob.server.retry.task.support.listener;

import com.aizuda.snailjob.common.core.alarm.AlarmContext;
import com.aizuda.snailjob.common.core.enums.RetryNotifySceneEnum;
import com.aizuda.snailjob.common.core.util.EnvironmentUtils;
import com.aizuda.snailjob.common.log.SnailJobLog;
import com.aizuda.snailjob.server.common.Lifecycle;
import com.aizuda.snailjob.server.common.alarm.AbstractRetryAlarm;
import com.aizuda.snailjob.server.common.dto.NotifyConfigInfo;
import com.aizuda.snailjob.server.common.dto.RetryAlarmInfo;
import com.aizuda.snailjob.server.common.enums.SyetemTaskTypeEnum;
import com.aizuda.snailjob.server.common.util.DateUtils;
import com.aizuda.snailjob.server.retry.task.dto.RetryTaskFailAlarmEventDTO;
import com.aizuda.snailjob.server.retry.task.support.RetryTaskConverter;
import com.aizuda.snailjob.server.retry.task.support.event.RetryTaskFailAlarmEvent;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * 重试任务失败监听器
 *
 * @author: zhengweilin
 * @date : 2024-12-13 16:57
 * @since 1.3.0
 */
@Component
@Slf4j
public class RetryTaskFailAlarmListener extends
        AbstractRetryAlarm<RetryTaskFailAlarmEvent> implements Runnable, Lifecycle {

    /**
     * 死信告警数据
     */
    private final LinkedBlockingQueue<RetryTaskFailAlarmEventDTO> queue = new LinkedBlockingQueue<>(1000);

    private static final String retryTaskDeadTextMessagesFormatter =
            "<font face=\"微软雅黑\" color=#ff0000 size=4>{}环境 重试任务执行失败</font>  \n" +
                    "> 任务重试次数:{}  \n" +
                    "> 通知场景:{}  \n" +
                    "> 空间ID:{}  \n" +
                    "> 组名称:{}  \n" +
                    "> 执行器名称:{}  \n" +
                    "> 场景名称:{}  \n" +
                    "> 业务数据:{}  \n" +
                    "> 时间:{}  \n" +
                    "> 失败原因:{}  \n";

    @Override
    protected List<SyetemTaskTypeEnum> getSystemTaskType() {
        return Lists.newArrayList(SyetemTaskTypeEnum.RETRY);
    }

    @Override
    protected List<RetryAlarmInfo> poll() throws InterruptedException {
        // 无数据时阻塞线程
        RetryTaskFailAlarmEventDTO retryTaskFailAlarmEventDO = queue.poll(100, TimeUnit.MILLISECONDS);
        if (Objects.isNull(retryTaskFailAlarmEventDO)) {
            return Lists.newArrayList();
        }

        // 拉取200条
        List<RetryTaskFailAlarmEventDTO> lists = Lists.newArrayList(retryTaskFailAlarmEventDO);
        queue.drainTo(lists, 200);

        // 数据类型转换
        return RetryTaskConverter.INSTANCE.toRetryTaskFailAlarmEventDTO(lists);
    }

    @Override
    @TransactionalEventListener(fallbackExecution = true, phase = TransactionPhase.AFTER_COMPLETION)
    public void doOnApplicationEvent(RetryTaskFailAlarmEvent retryTaskFailAlarmEvent) {
        if (!queue.offer(retryTaskFailAlarmEvent.getRetryTaskFailAlarmEventDTO())) {
            SnailJobLog.LOCAL.warn("任务重试失败告警队列已满");
        }
    }

    @Override
    protected AlarmContext buildAlarmContext(final RetryAlarmInfo retryAlarmInfo, final NotifyConfigInfo notifyConfig) {

        // 预警
        return AlarmContext.build().text(retryTaskDeadTextMessagesFormatter,
                        EnvironmentUtils.getActiveProfile(),
                        notifyConfig.getNotifyThreshold(),
                        RetryNotifySceneEnum.getRetryNotifyScene(retryAlarmInfo.getNotifyScene()).getDesc(),
                        retryAlarmInfo.getNamespaceId(),
                        retryAlarmInfo.getGroupName(),
                        retryAlarmInfo.getExecutorName(),
                        retryAlarmInfo.getSceneName(),
                        retryAlarmInfo.getArgsStr(),
                        DateUtils.toNowFormat(DateUtils.NORM_DATETIME_PATTERN),
                        retryAlarmInfo.getReason())
                .title("组:[{}] 场景:[{}] 环境重试任务失败",
                        retryAlarmInfo.getGroupName(), retryAlarmInfo.getSceneName());
    }

    @Override
    protected void startLog() {
        SnailJobLog.LOCAL.info("RetryTaskFailAlarmListener started");
    }

    @Override
    protected int getNotifyScene() {
        return RetryNotifySceneEnum.RETRY_TASK_FAIL_ERROR.getNotifyScene();
    }
}
