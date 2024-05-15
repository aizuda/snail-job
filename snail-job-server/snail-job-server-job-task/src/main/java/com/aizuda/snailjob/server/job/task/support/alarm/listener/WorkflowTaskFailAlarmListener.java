package com.aizuda.snailjob.server.job.task.support.alarm.listener;

import com.aizuda.snailjob.common.core.alarm.AlarmContext;
import com.aizuda.snailjob.common.core.enums.JobNotifySceneEnum;
import com.aizuda.snailjob.common.core.enums.JobOperationReasonEnum;
import com.aizuda.snailjob.common.core.util.EnvironmentUtils;
import com.aizuda.snailjob.common.log.SnailJobLog;
import com.aizuda.snailjob.server.common.AlarmInfoConverter;
import com.aizuda.snailjob.server.common.alarm.AbstractWorkflowAlarm;
import com.aizuda.snailjob.server.common.dto.NotifyConfigInfo;
import com.aizuda.snailjob.server.common.dto.WorkflowAlarmInfo;
import com.aizuda.snailjob.server.common.enums.SyetemTaskTypeEnum;
import com.aizuda.snailjob.server.common.util.DateUtils;
import com.aizuda.snailjob.server.job.task.support.alarm.event.WorkflowTaskFailAlarmEvent;
import com.aizuda.snailjob.template.datasource.persistence.dataobject.WorkflowBatchResponseDO;
import com.aizuda.snailjob.template.datasource.persistence.mapper.WorkflowTaskBatchMapper;
import com.aizuda.snailjob.template.datasource.persistence.po.JobTaskBatch;
import com.aizuda.snailjob.template.datasource.persistence.po.WorkflowTaskBatch;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

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
public class WorkflowTaskFailAlarmListener extends AbstractWorkflowAlarm<WorkflowTaskFailAlarmEvent> {

    private final WorkflowTaskBatchMapper workflowTaskBatchMapper;
    private final LinkedBlockingQueue<Long> queue = new LinkedBlockingQueue<>(1000);
    private static final String MESSAGES_FORMATTER = """
               <font face=微软雅黑 color=#ff0000 size=4>{}环境 Workflow任务执行失败</font>\s
                        > 空间ID:{} \s
                        > 组名称:{} \s
                        > 工作流名称:{} \s
                        > 失败原因:{} \s
                        > 时间:{};
            """;

    @Override
    protected List<WorkflowAlarmInfo> poll() throws InterruptedException {
        // 无数据时阻塞线程
        Long workflowTaskBatchId = queue.poll(100, TimeUnit.MILLISECONDS);
        if (Objects.isNull(workflowTaskBatchId)) {
            return Lists.newArrayList();
        }

        // 拉取200条
        List<Long> workflowTaskBatchIds = Lists.newArrayList(workflowTaskBatchId);
        queue.drainTo(workflowTaskBatchIds, 200);

        QueryWrapper<WorkflowTaskBatch> wrapper = new QueryWrapper<WorkflowTaskBatch>()
                .in("a.id", workflowTaskBatchIds).eq("a.deleted", 0);
        List<WorkflowBatchResponseDO> workflowTaskBatches = workflowTaskBatchMapper.selectWorkflowBatchList(wrapper);
        return AlarmInfoConverter.INSTANCE.toWorkflowAlarmInfos(workflowTaskBatches);
    }

    @Override
    protected AlarmContext buildAlarmContext(WorkflowAlarmInfo alarmDTO, NotifyConfigInfo notifyConfig) {
        String desc = JobOperationReasonEnum.getByReason(alarmDTO.getOperationReason()).getDesc();

        // 预警
        return AlarmContext.build()
                .text(MESSAGES_FORMATTER,
                        EnvironmentUtils.getActiveProfile(),
                        alarmDTO.getNamespaceId(),
                        alarmDTO.getGroupName(),
                        alarmDTO.getWorkflowName(),
                        desc,
                        DateUtils.toNowFormat(DateUtils.NORM_DATETIME_PATTERN))
                .title("{}环境 Workflow任务执行失败", EnvironmentUtils.getActiveProfile());
    }

    @Override
    protected void startLog() {
        SnailJobLog.LOCAL.info("WorkflowTaskFailAlarmListener started");
    }

    @Override
    protected int getNotifyScene() {
        return JobNotifySceneEnum.WORKFLOW_TASK_ERROR.getNotifyScene();
    }

    @Override
    protected List<SyetemTaskTypeEnum> getSystemTaskType() {
        return Lists.newArrayList(SyetemTaskTypeEnum.WORKFLOW);
    }

    @Override
    @TransactionalEventListener(fallbackExecution = true, phase = TransactionPhase.AFTER_COMPLETION)
    public void doOnApplicationEvent(WorkflowTaskFailAlarmEvent event) {
        if (!queue.offer(event.getWorkflowTaskBatchId())) {
            SnailJobLog.LOCAL.warn("Workflow任务执行失败告警队列已满");
        }
    }
}
