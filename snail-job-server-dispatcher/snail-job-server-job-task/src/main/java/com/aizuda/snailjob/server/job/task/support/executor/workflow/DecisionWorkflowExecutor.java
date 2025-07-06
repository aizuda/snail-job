package com.aizuda.snailjob.server.job.task.support.executor.workflow;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.aizuda.snailjob.common.core.constant.SystemConstants;
import com.aizuda.snailjob.common.core.context.SnailSpringContext;
import com.aizuda.snailjob.common.core.enums.*;
import com.aizuda.snailjob.common.core.expression.ExpressionEngine;
import com.aizuda.snailjob.common.core.expression.ExpressionFactory;
import com.aizuda.snailjob.common.core.util.JsonUtil;
import com.aizuda.snailjob.common.log.SnailJobLog;
import com.aizuda.snailjob.server.common.dto.DecisionConfig;
import com.aizuda.snailjob.server.common.dto.JobLogMetaDTO;
import com.aizuda.snailjob.server.common.enums.ExpressionTypeEnum;
import com.aizuda.snailjob.server.common.exception.SnailJobServerException;
import com.aizuda.snailjob.server.job.task.dto.WorkflowTaskFailAlarmEventDTO;
import com.aizuda.snailjob.server.job.task.support.alarm.event.WorkflowTaskFailAlarmEvent;
import com.aizuda.snailjob.server.job.task.support.expression.ExpressionInvocationHandler;
import com.aizuda.snailjob.template.datasource.persistence.mapper.WorkflowTaskBatchMapper;
import com.aizuda.snailjob.template.datasource.persistence.po.JobTask;
import com.aizuda.snailjob.template.datasource.persistence.po.JobTaskBatch;
import com.aizuda.snailjob.template.datasource.persistence.po.WorkflowTaskBatch;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Optional;

import static com.aizuda.snailjob.common.core.enums.JobOperationReasonEnum.WORKFLOW_SUCCESSOR_SKIP_EXECUTION;

/**
 * @author xiaowoniu
 * @date 2023-12-24 08:17:11
 * @since 2.6.0
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DecisionWorkflowExecutor extends AbstractWorkflowExecutor {
    private final WorkflowTaskBatchMapper workflowTaskBatchMapper;

    @Override
    public WorkflowNodeTypeEnum getWorkflowNodeType() {
        return WorkflowNodeTypeEnum.DECISION;
    }

    @Override
    protected void beforeExecute(WorkflowExecutorContext context) {

    }

    @Override
    public void doExecute(WorkflowExecutorContext context) {
        int taskBatchStatus = JobTaskBatchStatusEnum.SUCCESS.getStatus();
        int operationReason = JobOperationReasonEnum.NONE.getReason();
        int jobTaskStatus = JobTaskStatusEnum.SUCCESS.getStatus();
        String message = StrUtil.EMPTY;
        String wfContext = "";

        Boolean result = (Boolean) Optional.ofNullable(context.getEvaluationResult()).orElse(Boolean.FALSE);

        if (result || (WORKFLOW_SUCCESSOR_SKIP_EXECUTION.contains(context.getParentOperationReason()))) {
            // 多个条件节点直接是或的关系，只要一个成功其他节点就取消且是无需处理状态
            taskBatchStatus = JobTaskBatchStatusEnum.CANCEL.getStatus();
            jobTaskStatus = JobTaskStatusEnum.CANCEL.getStatus();
            operationReason = JobOperationReasonEnum.WORKFLOW_NODE_NO_REQUIRED.getReason();
        } else {
            DecisionConfig decisionConfig = JsonUtil.parseObject(context.getNodeInfo(), DecisionConfig.class);
            if (StatusEnum.NO.getStatus().equals(decisionConfig.getDefaultDecision())) {

                try {
                    // 这里重新加载一次最新的上下文
                    WorkflowTaskBatch workflowTaskBatch = workflowTaskBatchMapper.selectOne(new LambdaQueryWrapper<WorkflowTaskBatch>()
                            .select(WorkflowTaskBatch::getWfContext)
                            .eq(WorkflowTaskBatch::getId, context.getWorkflowTaskBatchId())
                    );

                    if (Objects.isNull(workflowTaskBatch)) {
                        operationReason = JobOperationReasonEnum.WORKFLOW_DECISION_FAILED.getReason();
                    } else {
                        wfContext = workflowTaskBatch.getWfContext();
                        ExpressionEngine realExpressionEngine = ExpressionTypeEnum.valueOf(decisionConfig.getExpressionType());
                        Assert.notNull(realExpressionEngine, () -> new SnailJobServerException("Expression engine does not exist"));
                        ExpressionInvocationHandler invocationHandler = new ExpressionInvocationHandler(realExpressionEngine);
                        ExpressionEngine expressionEngine = ExpressionFactory.getExpressionEngine(invocationHandler);
                        result = (Boolean) Optional.ofNullable(expressionEngine.eval(decisionConfig.getNodeExpression(), wfContext)).orElse(Boolean.FALSE);
                        if (!result) {
                            operationReason = JobOperationReasonEnum.WORKFLOW_DECISION_FAILED.getReason();
                        }
                    }

                } catch (Exception e) {
                    log.error("Condition expression execution parsing exception. Expression:[{}], Parameter: [{}]", decisionConfig.getNodeExpression(), wfContext, e);
                    taskBatchStatus = JobTaskBatchStatusEnum.FAIL.getStatus();
                    operationReason = JobOperationReasonEnum.WORKFLOW_CONDITION_NODE_EXECUTION_ERROR.getReason();
                    jobTaskStatus = JobTaskStatusEnum.FAIL.getStatus();
                    message = e.getMessage();

                    SnailSpringContext.getContext().publishEvent(new WorkflowTaskFailAlarmEvent(WorkflowTaskFailAlarmEventDTO.builder()
                            .workflowTaskBatchId(context.getWorkflowTaskBatchId())
                            .notifyScene(JobNotifySceneEnum.WORKFLOW_TASK_ERROR.getNotifyScene())
                            .reason(message)
                            .build()));
                }
            } else {
                result = Boolean.TRUE;
            }
        }

        // 回传执行结果
        context.setEvaluationResult(result);
        context.setTaskBatchStatus(taskBatchStatus);
        context.setOperationReason(operationReason);
        context.setJobTaskStatus(jobTaskStatus);
        context.setLogMessage(message);
        context.setWfContext(wfContext);

    }

    @Override
    protected boolean doPreValidate(WorkflowExecutorContext context) {
        return true;
    }

    @Override
    protected void afterExecute(WorkflowExecutorContext context) {

        JobTaskBatch jobTaskBatch = generateJobTaskBatch(context);

        JobTask jobTask = generateJobTask(context, jobTaskBatch);

        JobLogMetaDTO jobLogMetaDTO = new JobLogMetaDTO();
        jobLogMetaDTO.setNamespaceId(context.getNamespaceId());
        jobLogMetaDTO.setGroupName(context.getGroupName());
        jobLogMetaDTO.setTaskBatchId(jobTaskBatch.getId());
        jobLogMetaDTO.setJobId(SystemConstants.DECISION_JOB_ID);
        jobLogMetaDTO.setTaskId(jobTask.getId());
        if (jobTaskBatch.getTaskBatchStatus() == JobTaskStatusEnum.SUCCESS.getStatus()
                || JobOperationReasonEnum.WORKFLOW_NODE_NO_REQUIRED.getReason() == context.getOperationReason()) {
            SnailJobLog.REMOTE.info("Node ID:[{}] Decision completed. Context:[{}] Decision result:[{}] <|>{}<|>",
                    context.getWorkflowNodeId(), context.getWfContext(), context.getEvaluationResult(), jobLogMetaDTO);
        } else {
            SnailJobLog.REMOTE.error("Node ID:[{}] Decision failed. Context:[{}] Failure reason:[{}] <|>{}<|>",
                    context.getWorkflowNodeId(), context.getWfContext(), context.getLogMessage(), jobLogMetaDTO);

        }
    }
}
