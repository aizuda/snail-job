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
import com.aizuda.easy.retry.server.common.client.RequestInterceptor;
import com.aizuda.easy.retry.server.common.dto.CallbackConfig;
import com.aizuda.easy.retry.server.common.enums.ContentTypeEnum;
import com.aizuda.easy.retry.server.common.exception.EasyRetryServerException;
import com.aizuda.easy.retry.server.job.task.dto.JobLogDTO;
import com.aizuda.easy.retry.server.job.task.support.WorkflowTaskConverter;
import com.aizuda.easy.retry.server.job.task.support.generator.batch.JobTaskBatchGenerator;
import com.aizuda.easy.retry.server.job.task.support.generator.batch.JobTaskBatchGeneratorContext;
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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

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
    private final JobTaskBatchGenerator jobTaskBatchGenerator;

    @Override
    public WorkflowNodeTypeEnum getWorkflowNodeType() {
        return WorkflowNodeTypeEnum.CALLBACK;
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
            log.error("回调异常. webHook:[{}]，参数: [{}]", decisionConfig.getWebhook(), context.getResult(), e);
            taskBatchStatus = JobTaskBatchStatusEnum.FAIL.getStatus();
            operationReason = JobOperationReasonEnum.WORKFLOW_CONDITION_NODE_EXECUTOR_ERROR.getReason();
            jobTaskStatus = JobTaskStatusEnum.FAIL.getStatus();
            message = e.getMessage();
        }

        JobTaskBatchGeneratorContext generatorContext = WorkflowTaskConverter.INSTANCE.toJobTaskBatchGeneratorContext(context);
        generatorContext.setTaskBatchStatus(taskBatchStatus);
        generatorContext.setOperationReason(operationReason);
        generatorContext.setJobId(SystemConstants.CALLBACK_JOB_ID);
        JobTaskBatch jobTaskBatch = jobTaskBatchGenerator.generateJobTaskBatch(generatorContext);

        // 生成执行任务实例
        JobTask jobTask = new JobTask();
        jobTask.setGroupName(context.getGroupName());
        jobTask.setNamespaceId(context.getNamespaceId());
        jobTask.setJobId(SystemConstants.CALLBACK_JOB_ID);
        jobTask.setClientInfo(StrUtil.EMPTY);
        jobTask.setTaskBatchId(jobTaskBatch.getId());
        jobTask.setArgsType(1);
        jobTask.setArgsStr(Optional.ofNullable(context.getResult()).orElse(StrUtil.EMPTY));
        jobTask.setTaskStatus(jobTaskStatus);
        jobTask.setResultMessage(Optional.ofNullable(result).orElse(StrUtil.EMPTY));
        Assert.isTrue(1 == jobTaskMapper.insert(jobTask), () -> new EasyRetryServerException("新增任务实例失败"));

        // 保存执行的日志
        JobLogDTO jobLogDTO = new JobLogDTO();
        // TODO 等实时日志处理完毕后，再处理
        jobLogDTO.setMessage(message);
        jobLogDTO.setTaskId(jobTask.getId());
        jobLogDTO.setJobId(SystemConstants.CALLBACK_JOB_ID);
        jobLogDTO.setGroupName(context.getGroupName());
        jobLogDTO.setNamespaceId(context.getNamespaceId());
        jobLogDTO.setTaskBatchId(jobTaskBatch.getId());
        ActorRef actorRef = ActorGenerator.jobLogActor();
        actorRef.tell(jobLogDTO, actorRef);
    }
}
