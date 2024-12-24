package com.aizuda.snailjob.server.job.task.support.executor.workflow;

import cn.hutool.core.util.StrUtil;
import com.aizuda.snailjob.common.core.constant.SystemConstants;
import com.aizuda.snailjob.common.core.context.SnailSpringContext;
import com.aizuda.snailjob.common.core.enums.*;
import com.aizuda.snailjob.common.core.util.JsonUtil;
import com.aizuda.snailjob.common.log.SnailJobLog;
import com.aizuda.snailjob.server.common.dto.CallbackConfig;
import com.aizuda.snailjob.server.common.dto.JobLogMetaDTO;
import com.aizuda.snailjob.server.common.rpc.okhttp.RequestInterceptor;
import com.aizuda.snailjob.server.job.task.dto.WorkflowTaskFailAlarmEventDTO;
import com.aizuda.snailjob.server.job.task.support.alarm.event.WorkflowTaskFailAlarmEvent;
import com.aizuda.snailjob.server.model.dto.CallbackParamsDTO;
import com.aizuda.snailjob.template.datasource.persistence.po.JobTask;
import com.aizuda.snailjob.template.datasource.persistence.po.JobTaskBatch;
import com.github.rholder.retry.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static com.aizuda.snailjob.common.core.enums.JobOperationReasonEnum.WORKFLOW_SUCCESSOR_SKIP_EXECUTION;

/**
 * @author xiaowoniu
 * @date 2023-12-24 08:18:06
 * @since 2.6.0
 */
@Component
@RequiredArgsConstructor
public class CallbackWorkflowExecutor extends AbstractWorkflowExecutor {

    private static final String CALLBACK_TIMEOUT = "10";
    private final RestTemplate restTemplate;

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

        if (WORKFLOW_SUCCESSOR_SKIP_EXECUTION.contains(context.getParentOperationReason())) {
            // 针对无需处理的批次直接新增一个记录
            context.setTaskBatchStatus(JobTaskBatchStatusEnum.CANCEL.getStatus());
            context.setOperationReason(JobOperationReasonEnum.WORKFLOW_NODE_NO_REQUIRED.getReason());
            context.setJobTaskStatus(JobTaskStatusEnum.CANCEL.getStatus());
        } else if (Objects.equals(context.getWorkflowNodeStatus(), StatusEnum.NO.getStatus())) {
            context.setTaskBatchStatus(JobTaskBatchStatusEnum.CANCEL.getStatus());
            context.setOperationReason(JobOperationReasonEnum.WORKFLOW_NODE_CLOSED_SKIP_EXECUTION.getReason());
            context.setJobTaskStatus(JobTaskStatusEnum.CANCEL.getStatus());
        } else {
            invokeCallback(context);
        }

    }

    private void invokeCallback(WorkflowExecutorContext context) {

        CallbackConfig decisionConfig = JsonUtil.parseObject(context.getNodeInfo(), CallbackConfig.class);
        String message = StrUtil.EMPTY;
        String result = null;

        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.set(SystemConstants.SECRET, decisionConfig.getSecret());
        requestHeaders.setContentType(ContentTypeEnum.valueOf(decisionConfig.getContentType()).getMediaType());
        // 设置回调超时时间
        requestHeaders.set(RequestInterceptor.TIMEOUT_TIME, CALLBACK_TIMEOUT);

        CallbackParamsDTO callbackParamsDTO = new CallbackParamsDTO();
        callbackParamsDTO.setWfContext(context.getWfContext());

        try {
            Map<String, String> uriVariables = new HashMap<>();
            uriVariables.put(SystemConstants.SECRET, decisionConfig.getSecret());

            ResponseEntity<String> response = buildRetryer(decisionConfig).call(
                    () -> restTemplate.exchange(decisionConfig.getWebhook(), HttpMethod.POST,
                            new HttpEntity<>(callbackParamsDTO, requestHeaders), String.class, uriVariables));

            result = response.getBody();
            SnailJobLog.LOCAL.info("回调结果. webHook:[{}]，结果: [{}]", decisionConfig.getWebhook(), result);
        } catch (Exception e) {
            SnailJobLog.LOCAL.error("回调异常. webHook:[{}]，参数: [{}]", decisionConfig.getWebhook(),
                    context.getWfContext(), e);

            context.setTaskBatchStatus(JobTaskBatchStatusEnum.FAIL.getStatus());
            context.setOperationReason(JobOperationReasonEnum.WORKFLOW_CALLBACK_NODE_EXECUTION_ERROR.getReason());
            context.setJobTaskStatus(JobTaskStatusEnum.FAIL.getStatus());

            Throwable throwable = e;
            if (e.getClass().isAssignableFrom(RetryException.class)) {
                RetryException re = (RetryException) e;
                throwable = re.getLastFailedAttempt().getExceptionCause();
            }

            message = throwable.getMessage();
            SnailSpringContext.getContext().publishEvent(new WorkflowTaskFailAlarmEvent(WorkflowTaskFailAlarmEventDTO.builder()
                    .workflowTaskBatchId(context.getWorkflowTaskBatchId())
                    .notifyScene(JobNotifySceneEnum.WORKFLOW_TASK_ERROR.getNotifyScene())
                    .reason(message)
                    .build()));
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
                            SnailJobLog.LOCAL.error("回调接口第 【{}】 重试. 回调配置信息: [{}]",
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
            SnailJobLog.REMOTE.info("节点[{}]回调成功.\n回调参数:{} \n回调结果:[{}] <|>{}<|>",
                    context.getWorkflowNodeId(), context.getWfContext(), context.getEvaluationResult(), jobLogMetaDTO);
        } else if (jobTaskBatch.getTaskBatchStatus() == JobTaskStatusEnum.CANCEL.getStatus()) {
            if (WORKFLOW_SUCCESSOR_SKIP_EXECUTION.contains(context.getParentOperationReason())) {
                SnailJobLog.REMOTE.warn("节点[{}]取消回调. 取消原因: 当前任务无需处理 <|>{}<|>",
                        context.getWorkflowNodeId(), jobLogMetaDTO);
            } else {
                SnailJobLog.REMOTE.warn("节点[{}]取消回调. 取消原因: 任务状态已关闭 <|>{}<|>",
                        context.getWorkflowNodeId(), jobLogMetaDTO);
            }

        } else {
            SnailJobLog.REMOTE.error("节点[{}]回调失败.\n失败原因:{} <|>{}<|>",
                    context.getWorkflowNodeId(),
                    context.getLogMessage(), jobLogMetaDTO);
        }
    }
}
