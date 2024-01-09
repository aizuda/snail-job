package com.aizuda.easy.retry.server.job.task.support.executor.workflow;

import akka.actor.ActorRef;
import com.aizuda.easy.retry.common.core.enums.WorkflowNodeTypeEnum;
import com.aizuda.easy.retry.server.common.akka.ActorGenerator;
import com.aizuda.easy.retry.server.common.enums.JobTaskExecutorSceneEnum;
import com.aizuda.easy.retry.server.common.util.DateUtils;
import com.aizuda.easy.retry.server.job.task.dto.JobTaskPrepareDTO;
import com.aizuda.easy.retry.server.job.task.support.JobTaskConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * @author xiaowoniu
 * @date 2023-12-24 08:09:14
 * @since 2.6.0
 */
@Component
@RequiredArgsConstructor
public class JobTaskWorkflowExecutor extends AbstractWorkflowExecutor {

    @Override
    public WorkflowNodeTypeEnum getWorkflowNodeType() {
        return WorkflowNodeTypeEnum.JOB_TASK;
    }

    @Override
    protected boolean doPreValidate(WorkflowExecutorContext context) {
        return true;
    }

    @Override
    protected void afterExecute(WorkflowExecutorContext context) {

    }

    @Override
    protected void beforeExecute(WorkflowExecutorContext context) {

    }

    @Override
    protected void doExecute(WorkflowExecutorContext context) {
        // 生成任务批次
        JobTaskPrepareDTO jobTaskPrepare = JobTaskConverter.INSTANCE.toJobTaskPrepare(context.getJob());
        jobTaskPrepare.setTaskExecutorScene(JobTaskExecutorSceneEnum.AUTO_WORKFLOW.getType());
        jobTaskPrepare.setNextTriggerAt(DateUtils.toNowMilli() + DateUtils.toNowMilli() % 1000);
        jobTaskPrepare.setWorkflowNodeId(context.getWorkflowNodeId());
        jobTaskPrepare.setWorkflowTaskBatchId(context.getWorkflowTaskBatchId());
        jobTaskPrepare.setParentWorkflowNodeId(context.getParentWorkflowNodeId());
        // 执行预处理阶段
        ActorRef actorRef = ActorGenerator.jobTaskPrepareActor();
        actorRef.tell(jobTaskPrepare, actorRef);
    }
}
