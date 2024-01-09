package com.aizuda.easy.retry.server.job.task.support.timer;

import akka.actor.ActorRef;
import cn.hutool.core.lang.Assert;
import com.aizuda.easy.retry.common.core.constant.SystemConstants;
import com.aizuda.easy.retry.common.core.context.SpringContext;
import com.aizuda.easy.retry.common.core.enums.JobOperationReasonEnum;
import com.aizuda.easy.retry.common.core.enums.JobTaskBatchStatusEnum;
import com.aizuda.easy.retry.server.common.akka.ActorGenerator;
import com.aizuda.easy.retry.server.common.exception.EasyRetryServerException;
import com.aizuda.easy.retry.server.common.util.DateUtils;
import com.aizuda.easy.retry.server.job.task.dto.WorkflowNodeTaskExecuteDTO;
import com.aizuda.easy.retry.server.job.task.dto.WorkflowTimerTaskDTO;
import com.aizuda.easy.retry.template.datasource.persistence.mapper.WorkflowTaskBatchMapper;
import com.aizuda.easy.retry.template.datasource.persistence.po.WorkflowTaskBatch;
import io.netty.util.Timeout;
import io.netty.util.TimerTask;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

/**
 * @author: xiaowoniu
 * @date : 2023-09-25
 * @since 2.6.0
 */
@AllArgsConstructor
@Slf4j
public class WorkflowTimerTask implements TimerTask {

    private WorkflowTimerTaskDTO workflowTimerTaskDTO;

    @Override
    public void run(final Timeout timeout) throws Exception {
        // 执行任务调度
        log.info("开始执行任务调度. 当前时间:[{}] taskId:[{}]", LocalDateTime.now(), workflowTimerTaskDTO.getWorkflowTaskBatchId());

        try {

            int taskStatus = JobTaskBatchStatusEnum.RUNNING.getStatus();
            int operationReason = JobOperationReasonEnum.NONE.getReason();
            handlerTaskBatch(workflowTimerTaskDTO.getWorkflowTaskBatchId(), taskStatus, operationReason);

            WorkflowNodeTaskExecuteDTO taskExecuteDTO = new WorkflowNodeTaskExecuteDTO();
            taskExecuteDTO.setWorkflowTaskBatchId(workflowTimerTaskDTO.getWorkflowTaskBatchId());
            taskExecuteDTO.setWorkflowId(workflowTimerTaskDTO.getWorkflowId());
            taskExecuteDTO.setTaskExecutorScene(workflowTimerTaskDTO.getTaskExecutorScene());
            taskExecuteDTO.setParentId(SystemConstants.ROOT);
            ActorRef actorRef = ActorGenerator.workflowTaskExecutorActor();
            actorRef.tell(taskExecuteDTO, actorRef);

        } catch (Exception e) {
            log.error("任务调度执行失败", e);
        }
    }

    private void handlerTaskBatch(Long workflowTaskBatchId, int taskStatus, int operationReason) {

        WorkflowTaskBatch jobTaskBatch = new WorkflowTaskBatch();
        jobTaskBatch.setId(workflowTaskBatchId);
        jobTaskBatch.setExecutionAt(DateUtils.toNowMilli());
        jobTaskBatch.setTaskBatchStatus(taskStatus);
        jobTaskBatch.setOperationReason(operationReason);
        Assert.isTrue(1 ==  SpringContext.getBeanByType(WorkflowTaskBatchMapper.class).updateById(jobTaskBatch),
            () -> new EasyRetryServerException("更新任务失败"));

    }

}
