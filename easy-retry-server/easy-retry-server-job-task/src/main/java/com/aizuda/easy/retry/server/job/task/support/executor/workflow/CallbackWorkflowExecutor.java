package com.aizuda.easy.retry.server.job.task.support.executor.workflow;

import akka.actor.ActorRef;
import cn.hutool.core.util.StrUtil;
import com.aizuda.easy.retry.common.core.constant.SystemConstants;
import com.aizuda.easy.retry.common.core.enums.JobOperationReasonEnum;
import com.aizuda.easy.retry.common.core.enums.JobTaskBatchStatusEnum;
import com.aizuda.easy.retry.common.core.enums.JobTaskStatusEnum;
import com.aizuda.easy.retry.common.core.enums.WorkflowNodeTypeEnum;
import com.aizuda.easy.retry.common.core.util.JsonUtil;
import com.aizuda.easy.retry.server.common.akka.ActorGenerator;
import com.aizuda.easy.retry.server.common.client.RequestInterceptor;
import com.aizuda.easy.retry.server.common.dto.CallbackConfig;
import com.aizuda.easy.retry.server.common.enums.ContentTypeEnum;
import com.aizuda.easy.retry.server.job.task.dto.JobLogDTO;
import com.aizuda.easy.retry.server.job.task.support.WorkflowTaskConverter;
import com.aizuda.easy.retry.server.model.dto.CallbackParamsDTO;
import com.aizuda.easy.retry.template.datasource.persistence.mapper.JobTaskMapper;
import com.aizuda.easy.retry.template.datasource.persistence.po.JobTask;
import com.aizuda.easy.retry.template.datasource.persistence.po.JobTaskBatch;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author xiaowoniu
 * @date 2023-12-24 08:18:06
 * @since 2.6.0
 */
@Component
@RequiredArgsConstructor
@Slf4j
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

        CallbackConfig decisionConfig = JsonUtil.parseObject(context.getNodeInfo(), CallbackConfig.class);
        int taskBatchStatus = JobTaskBatchStatusEnum.SUCCESS.getStatus();
        int operationReason = JobOperationReasonEnum.NONE.getReason();
        int jobTaskStatus = JobTaskStatusEnum.SUCCESS.getStatus();

        String message = StrUtil.EMPTY;

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
        String result = null;
        try {
            Map<String, String> uriVariables = new HashMap<>();
            uriVariables.put(SECRET, decisionConfig.getSecret());
            // TODO 添加重试
            ResponseEntity<String> exchange = restTemplate.exchange(decisionConfig.getWebhook(), HttpMethod.POST,
                new HttpEntity<>(callbackParamsList, requestHeaders), String.class, uriVariables);
            result = exchange.getBody();
            log.info("回调结果. webHook:[{}]，参数: [{}]", decisionConfig.getWebhook(), result);
        } catch (Exception e) {
            log.error("回调异常. webHook:[{}]，参数: [{}]", decisionConfig.getWebhook(), context.getTaskResult(), e);
            taskBatchStatus = JobTaskBatchStatusEnum.FAIL.getStatus();
            operationReason = JobOperationReasonEnum.WORKFLOW_CALLBACK_NODE_EXECUTOR_ERROR.getReason();
            jobTaskStatus = JobTaskStatusEnum.FAIL.getStatus();
            message = e.getMessage();
        }

        if (JobTaskBatchStatusEnum.SUCCESS.getStatus() == taskBatchStatus) {
            workflowTaskExecutor(context);
        }

        context.setTaskBatchStatus(taskBatchStatus);
        context.setOperationReason(operationReason);
        context.setJobTaskStatus(jobTaskStatus);
        context.setEvaluationResult(result);
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

        // 保存执行的日志
        JobLogDTO jobLogDTO = new JobLogDTO();
        // TODO 等实时日志处理完毕后，再处理
        jobLogDTO.setMessage(context.getLogMessage());
        jobLogDTO.setTaskId(jobTask.getId());
        jobLogDTO.setJobId(SystemConstants.CALLBACK_JOB_ID);
        jobLogDTO.setGroupName(context.getGroupName());
        jobLogDTO.setNamespaceId(context.getNamespaceId());
        jobLogDTO.setTaskBatchId(jobTaskBatch.getId());
        ActorRef actorRef = ActorGenerator.jobLogActor();
        actorRef.tell(jobLogDTO, actorRef);
    }
}
