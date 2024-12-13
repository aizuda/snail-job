package com.aizuda.snailjob.server.retry.task.support.listener;

import cn.hutool.core.collection.CollUtil;
import com.aizuda.snailjob.common.core.alarm.AlarmContext;
import com.aizuda.snailjob.common.core.enums.RetryNotifySceneEnum;
import com.aizuda.snailjob.common.core.util.EnvironmentUtils;
import com.aizuda.snailjob.common.log.SnailJobLog;
import com.aizuda.snailjob.server.common.AlarmInfoConverter;
import com.aizuda.snailjob.server.common.Lifecycle;
import com.aizuda.snailjob.server.common.alarm.AbstractRetryAlarm;
import com.aizuda.snailjob.server.common.dto.JobAlarmInfo;
import com.aizuda.snailjob.server.common.dto.NotifyConfigInfo;
import com.aizuda.snailjob.server.common.dto.RetryAlarmInfo;
import com.aizuda.snailjob.server.common.enums.SyetemTaskTypeEnum;
import com.aizuda.snailjob.server.common.util.DateUtils;
import com.aizuda.snailjob.server.retry.task.dto.RetryTaskFailAlarmEventDTO;
import com.aizuda.snailjob.server.retry.task.support.event.RetryTaskFailAlarmEvent;
import com.aizuda.snailjob.server.retry.task.support.event.RetryTaskFailDeadLetterAlarmEvent;
import com.aizuda.snailjob.template.datasource.persistence.dataobject.JobBatchResponseDO;
import com.aizuda.snailjob.template.datasource.persistence.po.JobTaskBatch;
import com.aizuda.snailjob.template.datasource.persistence.po.RetryDeadLetter;
import com.aizuda.snailjob.template.datasource.persistence.po.RetryTask;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.Collections;
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

        RetryTaskFailAlarmEventDTO retryTaskFailAlarmEventDTO = queue.poll(100, TimeUnit.MILLISECONDS);
        if (Objects.isNull(retryTaskFailAlarmEventDTO)) {
            return Lists.newArrayList();
        }

        // 拉取200条
        /*List<Long> retryTaskIds = Lists.newArrayList(retryTaskFailAlarmEventDTO.getRetryTaskId());
        queue.drainTo(Collections.singleton(retryTaskIds), 200);
        QueryWrapper<RetryTask> wrapper = new QueryWrapper<RetryTask>()
                .in("batch.id", retryTaskIds)
                .eq("batch.deleted", 0);
        List<JobBatchResponseDO> jobTaskBatchList = jobTaskBatchMapper.selectJobBatchListByIds(wrapper);
        List<JobAlarmInfo> jobAlarmInfos = AlarmInfoConverter.INSTANCE.retryTaskToAlarmInfo(jobTaskBatchList);
        jobAlarmInfos.stream().forEach(i -> i.setReason(jobTaskFailAlarmEventDTO.getReason()));*/
        return null;
    }

    @Override
    @TransactionalEventListener(fallbackExecution = true, phase = TransactionPhase.AFTER_COMPLETION)
    public void doOnApplicationEvent(RetryTaskFailAlarmEvent retryTaskFailAlarmEvent) {
        if (!queue.offer(retryTaskFailAlarmEvent.getRetryTaskFailAlarmEventDTO())) {
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
                        DateUtils.format(retryAlarmInfo.getCreateDt(), DateUtils.NORM_DATETIME_PATTERN))
                .title("组:[{}] 场景:[{}] 环境重试任务失败",
                        retryAlarmInfo.getGroupName(), retryAlarmInfo.getSceneName());
    }

    @Override
    protected void startLog() {
        SnailJobLog.LOCAL.info("RetryTaskFailAlarmListener started");
    }

    @Override
    protected int getNotifyScene() {
        return RetryNotifySceneEnum.RETRY_NO_CLIENT_NODES_ERROR.getNotifyScene();
    }
}
