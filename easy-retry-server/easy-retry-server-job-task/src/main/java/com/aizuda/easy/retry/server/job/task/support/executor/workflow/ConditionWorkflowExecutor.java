package com.aizuda.easy.retry.server.job.task.support.executor.workflow;

import akka.actor.ActorRef;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.aizuda.easy.retry.common.core.constant.SystemConstants;
import com.aizuda.easy.retry.common.core.enums.JobOperationReasonEnum;
import com.aizuda.easy.retry.common.core.enums.JobTaskBatchStatusEnum;
import com.aizuda.easy.retry.common.core.enums.JobTaskStatusEnum;
import com.aizuda.easy.retry.common.core.enums.WorkflowNodeTypeEnum;
import com.aizuda.easy.retry.common.core.util.JsonUtil;
import com.aizuda.easy.retry.server.common.akka.ActorGenerator;
import com.aizuda.easy.retry.server.common.enums.JobTriggerTypeEnum;
import com.aizuda.easy.retry.server.common.exception.EasyRetryServerException;
import com.aizuda.easy.retry.server.common.util.ClientInfoUtils;
import com.aizuda.easy.retry.server.job.task.dto.JobLogDTO;
import com.aizuda.easy.retry.server.job.task.dto.WorkflowNodeTaskExecuteDTO;
import com.aizuda.easy.retry.server.job.task.support.JobTaskConverter;
import com.aizuda.easy.retry.server.job.task.support.WorkflowTaskConverter;
import com.aizuda.easy.retry.server.job.task.support.generator.batch.JobTaskBatchGenerator;
import com.aizuda.easy.retry.server.job.task.support.generator.batch.JobTaskBatchGeneratorContext;
import com.aizuda.easy.retry.template.datasource.persistence.mapper.JobTaskMapper;
import com.aizuda.easy.retry.template.datasource.persistence.po.JobTask;
import com.aizuda.easy.retry.template.datasource.persistence.po.JobTaskBatch;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;

/**
 * @author xiaowoniu
 * @date 2023-12-24 08:17:11
 * @since 2.6.0
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ConditionWorkflowExecutor extends AbstractWorkflowExecutor {

    private static final ExpressionParser ENGINE = new SpelExpressionParser();

    private final JobTaskBatchGenerator jobTaskBatchGenerator;
    private final JobTaskMapper jobTaskMapper;
    @Override
    public WorkflowNodeTypeEnum getWorkflowNodeType() {
        return WorkflowNodeTypeEnum.CONDITION;
    }

    @Override
    public void doExecute(WorkflowExecutorContext context) {
        int taskBatchStatus = JobTaskBatchStatusEnum.SUCCESS.getStatus();
        int operationReason = JobOperationReasonEnum.NONE.getReason();
        int jobTaskStatus = JobTaskStatusEnum.SUCCESS.getStatus();
        String message = StrUtil.EMPTY;

        try {
            // 根据配置的表达式执行
            Boolean result = doEval(context.getExpression(), JsonUtil.parseHashMap(context.getResult()));
            if (Boolean.TRUE.equals(result)) {
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
            jobTaskStatus = JobTaskStatusEnum.FAIL.getStatus();
            message = e.getMessage();
        }

        JobTaskBatchGeneratorContext generatorContext = WorkflowTaskConverter.INSTANCE.toJobTaskBatchGeneratorContext(context);
        generatorContext.setTaskBatchStatus(taskBatchStatus);
        generatorContext.setOperationReason(operationReason);
        generatorContext.setJobId(SystemConstants.CONDITION_JOB_ID);
        JobTaskBatch jobTaskBatch = jobTaskBatchGenerator.generateJobTaskBatch(generatorContext);

        // 生成执行任务实例
        JobTask jobTask = new JobTask();
        jobTask.setGroupName(context.getGroupName());
        jobTask.setNamespaceId(context.getNamespaceId());
        jobTask.setJobId(SystemConstants.CONDITION_JOB_ID);
        jobTask.setClientInfo(StrUtil.EMPTY);
        jobTask.setTaskBatchId(jobTaskBatch.getId());
        jobTask.setArgsType(1);
        jobTask.setArgsStr(context.getExpression());
        jobTask.setTaskStatus(jobTaskStatus);
        jobTask.setResultMessage(Optional.ofNullable(context.getResult()).orElse(StrUtil.EMPTY));
        Assert.isTrue(1 == jobTaskMapper.insert(jobTask), () -> new EasyRetryServerException("新增任务实例失败"));

        // 保存执行的日志
        JobLogDTO jobLogDTO = new JobLogDTO();
        jobLogDTO.setMessage(message);
        jobLogDTO.setTaskId(jobTask.getId());
        jobLogDTO.setJobId(SystemConstants.CONDITION_JOB_ID);
        jobLogDTO.setGroupName(context.getGroupName());
        jobLogDTO.setNamespaceId(context.getNamespaceId());
        jobLogDTO.setTaskBatchId(jobTaskBatch.getId());
        ActorRef actorRef = ActorGenerator.jobLogActor();
        actorRef.tell(jobLogDTO, actorRef);
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
