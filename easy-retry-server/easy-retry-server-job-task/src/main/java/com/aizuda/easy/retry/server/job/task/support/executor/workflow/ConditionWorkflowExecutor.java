package com.aizuda.easy.retry.server.job.task.support.executor.workflow;

import akka.actor.ActorRef;
import com.aizuda.easy.retry.common.core.enums.JobOperationReasonEnum;
import com.aizuda.easy.retry.common.core.enums.JobTaskBatchStatusEnum;
import com.aizuda.easy.retry.common.core.enums.WorkflowNodeTypeEnum;
import com.aizuda.easy.retry.common.core.util.JsonUtil;
import com.aizuda.easy.retry.server.common.akka.ActorGenerator;
import com.aizuda.easy.retry.server.common.enums.JobTriggerTypeEnum;
import com.aizuda.easy.retry.server.common.exception.EasyRetryServerException;
import com.aizuda.easy.retry.server.common.util.DateUtils;
import com.aizuda.easy.retry.server.job.task.dto.JobTaskPrepareDTO;
import com.aizuda.easy.retry.server.job.task.support.JobTaskConverter;
import com.aizuda.easy.retry.server.job.task.support.generator.batch.JobTaskBatchGenerator;
import com.aizuda.easy.retry.server.job.task.support.generator.batch.JobTaskBatchGeneratorContext;
import com.aizuda.easy.retry.template.datasource.persistence.mapper.JobTaskBatchMapper;
import com.aizuda.easy.retry.template.datasource.persistence.po.JobTaskBatch;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
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
public class ConditionWorkflowExecutor extends AbstractWorkflowExecutor {

    private final static ExpressionParser ENGINE = new SpelExpressionParser();

    private final JobTaskBatchGenerator jobTaskBatchGenerator;

    @Override
    public WorkflowNodeTypeEnum getWorkflowNodeType() {
        return WorkflowNodeTypeEnum.CONDITION;
    }

    @Override
    protected void doExecute(WorkflowExecutorContext context) {

        // 根据配置的表达式执行
        Boolean result = doEval(context.getExpression(), context.getExpressionContext());
        if (result) {
            JobTaskBatchGeneratorContext generatorContext = new JobTaskBatchGeneratorContext();
            generatorContext.setGroupName(context.getGroupName());
            generatorContext.setNamespaceId(context.getNamespaceId());
            generatorContext.setTaskBatchStatus(JobTaskBatchStatusEnum.SUCCESS.getStatus());
            generatorContext.setOperationReason(JobOperationReasonEnum.NONE.getReason());
            // 特殊的job
            generatorContext.setJobId(-1L);
            generatorContext.setWorkflowNodeId(context.getWorkflowNodeId());
            generatorContext.setParentWorkflowNodeId(context.getParentWorkflowNodeId());
            generatorContext.setWorkflowTaskBatchId(context.getWorkflowTaskBatchId());
            jobTaskBatchGenerator.generateJobTaskBatch(generatorContext);
        } else {
            //
        }

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
