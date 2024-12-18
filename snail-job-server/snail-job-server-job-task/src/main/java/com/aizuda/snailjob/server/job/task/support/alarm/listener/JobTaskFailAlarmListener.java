package com.aizuda.snailjob.server.job.task.support.alarm.listener;

import com.aizuda.snailjob.common.core.alarm.AlarmContext;
import com.aizuda.snailjob.common.core.enums.JobNotifySceneEnum;
import com.aizuda.snailjob.common.core.enums.JobOperationReasonEnum;
import com.aizuda.snailjob.common.core.util.EnvironmentUtils;
import com.aizuda.snailjob.common.log.SnailJobLog;
import com.aizuda.snailjob.server.common.AlarmInfoConverter;
import com.aizuda.snailjob.server.common.alarm.AbstractJobAlarm;
import com.aizuda.snailjob.server.common.dto.JobAlarmInfo;
import com.aizuda.snailjob.server.common.dto.NotifyConfigInfo;
import com.aizuda.snailjob.server.common.enums.SyetemTaskTypeEnum;
import com.aizuda.snailjob.server.common.util.DateUtils;
import com.aizuda.snailjob.server.job.task.dto.JobTaskFailAlarmEventDTO;
import com.aizuda.snailjob.server.job.task.support.alarm.event.JobTaskFailAlarmEvent;
import com.aizuda.snailjob.template.datasource.persistence.dataobject.JobBatchResponseDO;
import com.aizuda.snailjob.template.datasource.persistence.mapper.JobTaskBatchMapper;
import com.aizuda.snailjob.template.datasource.persistence.po.JobTaskBatch;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;


/**
 * JOB任务执行失败告警
 *
 * @author: zuoJunLin
 * @date : 2023-12-02 21:40
 * @since 2.5.0
 */
@Component
@RequiredArgsConstructor
public class JobTaskFailAlarmListener extends AbstractJobAlarm<JobTaskFailAlarmEvent> {

    private final JobTaskBatchMapper jobTaskBatchMapper;

    /**
     * job任务失败数据
     */
    private final LinkedBlockingQueue<JobTaskFailAlarmEventDTO> queue = new LinkedBlockingQueue<>(1000);

    private static final String MESSAGES_FORMATTER = """
               <font face=微软雅黑 color=#ff0000 size=4>{}环境 Job任务执行失败</font>\s
                        > 空间ID:{} \s
                        > 组名称:{} \s
                        > 任务名称:{} \s
                        > 执行器名称:{} \s
                        > 通知场景:{} \s
                        > 失败原因:{} \s
                        > 方法参数:{} \s
                        > 时间:{};
            """;

    @Override
    protected List<JobAlarmInfo> poll() throws InterruptedException {
        // 无数据时阻塞线程
        JobTaskFailAlarmEventDTO jobTaskFailAlarmEventDTO = queue.poll(100, TimeUnit.MILLISECONDS);
        if (Objects.isNull(jobTaskFailAlarmEventDTO)) {
            return Lists.newArrayList();
        }

        // 拉取200条
        List<Long> jobTaskBatchIds = Lists.newArrayList(jobTaskFailAlarmEventDTO.getJobTaskBatchId());
        queue.drainTo(Collections.singleton(jobTaskBatchIds), 200);
        QueryWrapper<JobTaskBatch> wrapper = new QueryWrapper<JobTaskBatch>()
                .in("batch.id", jobTaskBatchIds)
                .eq("batch.deleted", 0);
        List<JobBatchResponseDO> jobTaskBatchList = jobTaskBatchMapper.selectJobBatchListByIds(wrapper);
        return AlarmInfoConverter.INSTANCE.toJobAlarmInfos(jobTaskBatchList);
    }

    @Override
    protected AlarmContext buildAlarmContext(JobAlarmInfo alarmDTO, NotifyConfigInfo notifyConfig) {

        // 预警
        return AlarmContext.build()
                .text(MESSAGES_FORMATTER,
                        EnvironmentUtils.getActiveProfile(),
                        alarmDTO.getNamespaceId(),
                        alarmDTO.getGroupName(),
                        alarmDTO.getJobName(),
                        alarmDTO.getExecutorInfo(),
                        JobOperationReasonEnum.getByReason(alarmDTO.getOperationReason()).getDesc(),
                        alarmDTO.getReason(),
                        alarmDTO.getArgsStr(),
                        DateUtils.toNowFormat(DateUtils.NORM_DATETIME_PATTERN))
                .title("{}环境 JOB任务失败", EnvironmentUtils.getActiveProfile());
    }

    @Override
    protected void startLog() {
        SnailJobLog.LOCAL.info("JobTaskFailAlarmListener started");
    }

    @Override
    protected int getNotifyScene() {
        return JobNotifySceneEnum.JOB_TASK_ERROR.getNotifyScene();
    }

    @Override
    protected List<SyetemTaskTypeEnum> getSystemTaskType() {
        return Lists.newArrayList(SyetemTaskTypeEnum.JOB);
    }

    @Override
    @TransactionalEventListener(fallbackExecution = true, phase = TransactionPhase.AFTER_COMPLETION)
    public void doOnApplicationEvent(JobTaskFailAlarmEvent jobTaskFailAlarmEvent) {
        if (!queue.offer(jobTaskFailAlarmEvent.getJobTaskFailAlarmEventDTO())) {
            SnailJobLog.LOCAL.warn("JOB任务执行失败告警队列已满");
        }
    }
}
