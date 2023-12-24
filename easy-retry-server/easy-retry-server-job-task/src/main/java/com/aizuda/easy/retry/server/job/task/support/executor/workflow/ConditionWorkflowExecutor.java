package com.aizuda.easy.retry.server.job.task.support.executor.workflow;

import akka.actor.ActorRef;
import com.aizuda.easy.retry.common.core.constant.SystemConstants;
import com.aizuda.easy.retry.common.core.enums.JobOperationReasonEnum;
import com.aizuda.easy.retry.common.core.enums.JobTaskBatchStatusEnum;
import com.aizuda.easy.retry.common.core.enums.WorkflowNodeTypeEnum;
import com.aizuda.easy.retry.common.core.util.JsonUtil;
import com.aizuda.easy.retry.server.common.akka.ActorGenerator;
import com.aizuda.easy.retry.server.common.enums.JobTriggerTypeEnum;
import com.aizuda.easy.retry.server.common.exception.EasyRetryServerException;
import com.aizuda.easy.retry.server.job.task.dto.WorkflowNodeTaskExecuteDTO;
import com.aizuda.easy.retry.server.job.task.support.WorkflowTaskConverter;
import com.aizuda.easy.retry.server.job.task.support.generator.batch.JobTaskBatchGenerator;
import com.aizuda.easy.retry.server.job.task.support.generator.batch.JobTaskBatchGeneratorContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author xiaowoniu
 * @date 2023-12-24 08:17:11
 * @since 2.6.0
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ConditionWorkflowExecutor extends AbstractWorkflowExecutor {

    private final static ExpressionParser ENGINE = new SpelExpressionParser();

    private final JobTaskBatchGenerator jobTaskBatchGenerator;

    @Override
    public WorkflowNodeTypeEnum getWorkflowNodeType() {
        return WorkflowNodeTypeEnum.CONDITION;
    }

    @Override
    protected void doExecute(WorkflowExecutorContext context) {
        int taskBatchStatus = JobTaskBatchStatusEnum.SUCCESS.getStatus();
        int operationReason = JobOperationReasonEnum.NONE.getReason();

        try {
            // 根据配置的表达式执行
            Boolean result = doEval(context.getExpression(), JsonUtil.parseHashMap(context.getResult()));
            if (result) {
                // 若是工作流则开启下一个任务
                try {
                    WorkflowNodeTaskExecuteDTO taskExecuteDTO = new WorkflowNodeTaskExecuteDTO();
                    taskExecuteDTO.setWorkflowTaskBatchId(context.getWorkflowTaskBatchId());
                    taskExecuteDTO.setTriggerType(JobTriggerTypeEnum.AUTO.getType());
                    taskExecuteDTO.setParentId(context.getWorkflowNodeId());
                    taskExecuteDTO.setResult(context.getResult());
                    ActorRef actorRef = ActorGenerator.workflowTaskExecutorActor();
                    actorRef.tell(taskExecuteDTO, actorRef);
                } catch (Exception e) {
                    log.error("工作流执行失败", e);
                }
            }
        } catch (Exception e) {
            taskBatchStatus = JobTaskBatchStatusEnum.FAIL.getStatus();
            operationReason = JobOperationReasonEnum.WORKFLOW_CONDITION_NODE_EXECUTOR_ERROR.getReason();
        }

        JobTaskBatchGeneratorContext generatorContext = WorkflowTaskConverter.INSTANCE.toJobTaskBatchGeneratorContext(context);
        generatorContext.setTaskBatchStatus(taskBatchStatus);
        generatorContext.setOperationReason(operationReason);

        // 特殊的job
        generatorContext.setJobId(SystemConstants.CONDITION_JOB_ID);
        jobTaskBatchGenerator.generateJobTaskBatch(generatorContext);
    }

    protected Boolean doEval(String expression, Map<String, Object> context) {
        try {
            final EvaluationContext evaluationContext = new StandardEvaluationContext();
            context.forEach(evaluationContext::setVariable);
            return ENGINE.parseExpression(expression).getValue(evaluationContext, Boolean.class);
        } catch (Exception e) {
            throw new EasyRetryServerException("SpEL表达式解析异常. expression:[{}] context:[{}]",
                    expression, JsonUtil.toJsonString(context), e);
        }

    }
}
