package com.aizuda.easy.retry.server.job.task.support.executor.workflow;

import cn.hutool.core.util.StrUtil;
import com.aizuda.easy.retry.common.core.constant.SystemConstants;
import com.aizuda.easy.retry.common.core.enums.JobOperationReasonEnum;
import com.aizuda.easy.retry.common.core.enums.JobTaskBatchStatusEnum;
import com.aizuda.easy.retry.common.core.enums.JobTaskStatusEnum;
import com.aizuda.easy.retry.common.core.enums.StatusEnum;
import com.aizuda.easy.retry.common.core.enums.WorkflowNodeTypeEnum;
import com.aizuda.easy.retry.common.core.util.JsonUtil;
import com.aizuda.easy.retry.common.log.EasyRetryLog;
import com.aizuda.easy.retry.server.common.client.RequestInterceptor;
import com.aizuda.easy.retry.server.common.dto.CallbackConfig;
import com.aizuda.easy.retry.server.common.enums.ContentTypeEnum;
import com.aizuda.easy.retry.server.common.dto.JobLogMetaDTO;
import com.aizuda.easy.retry.server.job.task.support.WorkflowTaskConverter;
import com.aizuda.easy.retry.server.model.dto.CallbackParamsDTO;
import com.aizuda.easy.retry.template.datasource.persistence.mapper.JobTaskMapper;
import com.aizuda.easy.retry.template.datasource.persistence.po.JobTask;
import com.aizuda.easy.retry.template.datasource.persistence.po.JobTaskBatch;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.rholder.retry.Attempt;
import com.github.rholder.retry.RetryException;
import com.github.rholder.retry.RetryListener;
import com.github.rholder.retry.Retryer;
import com.github.rholder.retry.RetryerBuilder;
import com.github.rholder.retry.StopStrategies;
import com.github.rholder.retry.WaitStrategies;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * @author xiaowoniu
 * @date 2023-12-24 08:18:06
 * @since 2.6.0
 */
@Component
@RequiredArgsConstructor
public class CallbackWorkflowExecutor extends AbstractWorkflowExecutor {

    private static final String SECRET = "secret";
    private static final String CALLBACK_TIMEOUT = "10";
    private final RestTemplate restTemplate;
    private final JobTaskMapper jobTaskMapper;

    @Override
    public WorkflowNodeTypeEnum getWorkflowNodeType() {
        return WorkflowNodeTypeEnum.CALLBACK;
    }

    @Override
    protected void beforeExecute(WorkflowExecutorContext context) {

    }

    @Override
    protected void doExecute(WorkflowExecutorContext context) {

        // 初始化上下状态
        context.setTaskBatchStatus(JobTaskBatchStatusEnum.SUCCESS.getStatus());
        context.setOperationReason(JobOperationReasonEnum.NONE.getReason());
        context.setJobTaskStatus(JobTaskStatusEnum.SUCCESS.getStatus());

        if (Objects.equals(context.getWorkflowNodeStatus(), StatusEnum.NO.getStatus())) {
            context.setTaskBatchStatus(JobTaskBatchStatusEnum.CANCEL.getStatus());
            context.setOperationReason(JobOperationReasonEnum.WORKFLOW_NODE_CLOSED_SKIP_EXECUTION.getReason());
            context.setJobTaskStatus(JobTaskStatusEnum.CANCEL.getStatus());
        } else {
            invokeCallback(context);
        }

        // 执行下一个节点
        workflowTaskExecutor(context);

    }

    private void invokeCallback(WorkflowExecutorContext context) {

        CallbackConfig decisionConfig = JsonUtil.parseObject(context.getNodeInfo(), CallbackConfig.class);
        String message = StrUtil.EMPTY;
        String result = null;

        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.set(SECRET, decisionConfig.getSecret());
        requestHeaders.setContentType(ContentTypeEnum.valueOf(decisionConfig.getContentType()).getMediaType());
        // 设置回调超时时间
        requestHeaders.set(RequestInterceptor.TIMEOUT_TIME, CALLBACK_TIMEOUT);

        List<JobTask> jobTasks = jobTaskMapper.selectList(new LambdaQueryWrapper<JobTask>()
                .select(JobTask::getResultMessage, JobTask::getClientInfo)
                .eq(JobTask::getTaskBatchId, context.getTaskBatchId()));
        List<CallbackParamsDTO> callbackParamsList = WorkflowTaskConverter.INSTANCE.toCallbackParamsDTO(jobTasks);

        context.setTaskResult(JsonUtil.toJsonString(callbackParamsList));

        try {
            Map<String, String> uriVariables = new HashMap<>();
            uriVariables.put(SECRET, decisionConfig.getSecret());

            ResponseEntity<String> response = buildRetryer(decisionConfig).call(
                    () -> restTemplate.exchange(decisionConfig.getWebhook(), HttpMethod.POST,
                            new HttpEntity<>(callbackParamsList, requestHeaders), String.class, uriVariables));

            result = response.getBody();
            EasyRetryLog.LOCAL.info("回调结果. webHook:[{}]，结果: [{}]", decisionConfig.getWebhook(), result);
        } catch (Exception e) {
            EasyRetryLog.LOCAL.error("回调异常. webHook:[{}]，参数: [{}]", decisionConfig.getWebhook(),
                    context.getTaskResult(), e);

            context.setTaskBatchStatus(JobTaskBatchStatusEnum.FAIL.getStatus());
            context.setOperationReason(JobOperationReasonEnum.WORKFLOW_CALLBACK_NODE_EXECUTION_ERROR.getReason());
            context.setJobTaskStatus(JobTaskStatusEnum.FAIL.getStatus());

            Throwable throwable = e;
            if (e.getClass().isAssignableFrom(RetryException.class)) {
                RetryException re = (RetryException) e;
                throwable = re.getLastFailedAttempt().getExceptionCause();
            }

            message = throwable.getMessage();
        }

        context.setEvaluationResult(result);
        context.setLogMessage(message);

    }

    private static Retryer<ResponseEntity<String>> buildRetryer(CallbackConfig decisionConfig) {
        Retryer<ResponseEntity<String>> retryer = RetryerBuilder.<ResponseEntity<String>>newBuilder()
                .retryIfException(throwable -> true)
                .withWaitStrategy(WaitStrategies.fixedWait(150, TimeUnit.MILLISECONDS))
                .withStopStrategy(StopStrategies.stopAfterAttempt(10))
                .withRetryListener(new RetryListener() {
                    @Override
                    public <V> void onRetry(final Attempt<V> attempt) {
                        if (attempt.hasException()) {
                            EasyRetryLog.LOCAL.error("回调接口第 【{}】 重试. 回调配置信息: [{}]",
                                    attempt.getAttemptNumber(), JsonUtil.toJsonString(decisionConfig));
                        }
                    }
                }).build();
        return retryer;
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
        jobLogMetaDTO.setJobId(SystemConstants.CALLBACK_JOB_ID);
        jobLogMetaDTO.setTaskId(jobTask.getId());
        if (jobTaskBatch.getTaskBatchStatus() == JobTaskStatusEnum.SUCCESS.getStatus()) {
            EasyRetryLog.REMOTE.info("节点[{}]回调成功.\n回调参数:{} \n回调结果:[{}] <|>{}<|>",
                    context.getWorkflowNodeId(), context.getTaskResult(), context.getEvaluationResult(), jobLogMetaDTO);
        } else if (jobTaskBatch.getTaskBatchStatus() == JobTaskStatusEnum.CANCEL.getStatus()) {
            EasyRetryLog.REMOTE.warn("节点[{}]取消回调. 取消原因: 任务状态已关闭 <|>{}<|>",
                    context.getWorkflowNodeId(), jobLogMetaDTO);
        } else {
            EasyRetryLog.REMOTE.error("节点[{}]回调失败.\n失败原因:{} <|>{}<|>",
                    context.getWorkflowNodeId(),
                    context.getLogMessage(), jobLogMetaDTO);
        }
    }
}
