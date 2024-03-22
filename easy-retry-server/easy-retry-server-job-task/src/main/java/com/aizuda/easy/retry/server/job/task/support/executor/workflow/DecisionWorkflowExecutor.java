package com.aizuda.easy.retry.server.job.task.support.executor.workflow;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.aizuda.easy.retry.common.core.constant.SystemConstants;
import com.aizuda.easy.retry.common.core.enums.*;
import com.aizuda.easy.retry.common.core.expression.ExpressionEngine;
import com.aizuda.easy.retry.common.core.expression.ExpressionFactory;
import com.aizuda.easy.retry.common.core.util.JsonUtil;
import com.aizuda.easy.retry.common.log.EasyRetryLog;
import com.aizuda.easy.retry.server.common.dto.DecisionConfig;
import com.aizuda.easy.retry.server.common.enums.ExpressionTypeEnum;
import com.aizuda.easy.retry.server.common.enums.LogicalConditionEnum;
import com.aizuda.easy.retry.server.common.exception.EasyRetryServerException;
import com.aizuda.easy.retry.server.job.task.dto.JobLogMetaDTO;
import com.aizuda.easy.retry.server.job.task.support.expression.ExpressionInvocationHandler;
import com.aizuda.easy.retry.template.datasource.persistence.mapper.JobTaskMapper;
import com.aizuda.easy.retry.template.datasource.persistence.po.JobTask;
import com.aizuda.easy.retry.template.datasource.persistence.po.JobTaskBatch;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * @author xiaowoniu
 * @date 2023-12-24 08:17:11
 * @since 2.6.0
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DecisionWorkflowExecutor extends AbstractWorkflowExecutor {
    private final JobTaskMapper jobTaskMapper;

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

        Boolean result = (Boolean) Optional.ofNullable(context.getEvaluationResult()).orElse(Boolean.FALSE);

        if (result) {
            // 多个条件节点直接是或的关系，只要一个成功其他节点就取消且是无需处理状态
            taskBatchStatus = JobTaskBatchStatusEnum.CANCEL.getStatus();
            jobTaskStatus = JobTaskStatusEnum.CANCEL.getStatus();
            operationReason = JobOperationReasonEnum.WORKFLOW_NODE_NO_REQUIRED.getReason();
        } else {
            DecisionConfig decisionConfig = JsonUtil.parseObject(context.getNodeInfo(), DecisionConfig.class);
            if (StatusEnum.NO.getStatus().equals(decisionConfig.getDefaultDecision())) {
                try {
                    ExpressionEngine realExpressionEngine = ExpressionTypeEnum.valueOf(decisionConfig.getExpressionType());
                    Assert.notNull(realExpressionEngine, () -> new EasyRetryServerException("表达式引擎不存在"));
                    ExpressionInvocationHandler invocationHandler = new ExpressionInvocationHandler(realExpressionEngine);
                    ExpressionEngine expressionEngine = ExpressionFactory.getExpressionEngine(invocationHandler);

                    List<JobTask> jobTasks = jobTaskMapper.selectList(new LambdaQueryWrapper<JobTask>()
                            .select(JobTask::getResultMessage)
                            .eq(JobTask::getTaskBatchId, context.getTaskBatchId()));
                    List<String> taskResult = Lists.newArrayList();
                    Boolean tempResult = null;
                    if (CollectionUtils.isEmpty(jobTasks)) {
                        tempResult = (Boolean) Optional.ofNullable(expressionEngine.eval(decisionConfig.getNodeExpression(), StrUtil.EMPTY)).orElse(Boolean.FALSE);
                    } else {
                        for (JobTask jobTask : jobTasks) {
                            taskResult.add(jobTask.getResultMessage());
                            boolean execResult = (Boolean) Optional.ofNullable(expressionEngine.eval(decisionConfig.getNodeExpression(), jobTask.getResultMessage())).orElse(Boolean.FALSE);

                            if (Objects.isNull(tempResult)) {
                                tempResult = execResult;
                            }

                            if (Objects.equals(decisionConfig.getLogicalCondition(), LogicalConditionEnum.AND.getCode())) {
                                tempResult = tempResult && execResult;
                            } else {
                                tempResult = tempResult || execResult;
                                if (tempResult) {
                                    break;
                                }
                            }

                            log.debug("执行条件表达式：[{}]，参数: [{}] 结果：[{}]", decisionConfig.getNodeExpression(), jobTask.getResultMessage(), result);
                        }
                    }

                    context.setTaskResult(JsonUtil.toJsonString(taskResult));
                    result = Optional.ofNullable(tempResult).orElse(Boolean.FALSE);
                    if (!result) {
                        operationReason = JobOperationReasonEnum.WORKFLOW_DECISION_FAILED.getReason();
                    }
                } catch (Exception e) {
                    log.error("执行条件表达式解析异常. 表达式:[{}]，参数: [{}]", decisionConfig.getNodeExpression(), context.getTaskResult(), e);
                    taskBatchStatus = JobTaskBatchStatusEnum.FAIL.getStatus();
                    operationReason = JobOperationReasonEnum.WORKFLOW_CONDITION_NODE_EXECUTION_ERROR.getReason();
                    jobTaskStatus = JobTaskStatusEnum.FAIL.getStatus();
                    message = e.getMessage();
                }
            } else {
                result = Boolean.TRUE;
            }
        }

        if (JobTaskBatchStatusEnum.SUCCESS.getStatus() == taskBatchStatus && result) {
            workflowTaskExecutor(context);
        }

        // 回传执行结果
        context.setEvaluationResult(result);
        context.setTaskBatchStatus(taskBatchStatus);
        context.setOperationReason(operationReason);
        context.setJobTaskStatus(jobTaskStatus);
        context.setLogMessage(message);

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
            EasyRetryLog.REMOTE.info("节点Id:[{}] 决策完成. 上下文:[{}] 决策结果:[{}] <|>{}<|>",
                context.getWorkflowNodeId(), context.getTaskResult(), context.getEvaluationResult(), jobLogMetaDTO);
        } else {
            EasyRetryLog.REMOTE.error("节点Id:[{}] 决策失败. 上下文:[{}] 失败原因:[{}] <|>{}<|>",
                context.getWorkflowNodeId(), context.getTaskResult(), context.getLogMessage(), jobLogMetaDTO);

        }
    }
}
