package com.aizuda.easy.retry.server.job.task.support.executor.workflow;

import com.aizuda.easy.retry.common.core.enums.WorkflowNodeTypeEnum;
import com.aizuda.easy.retry.common.core.util.JsonUtil;
import com.aizuda.easy.retry.server.common.dto.CallbackConfig;
import com.aizuda.easy.retry.template.datasource.persistence.mapper.JobTaskMapper;
import com.aizuda.easy.retry.template.datasource.persistence.po.JobTask;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
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
public class CallbackWorkflowExecutor extends AbstractWorkflowExecutor {
    private final RestTemplate restTemplate;
    private final JobTaskMapper jobTaskMapper;
    @Override
    public WorkflowNodeTypeEnum getWorkflowNodeType() {
        return WorkflowNodeTypeEnum.CALLBACK;
    }

    @Override
    protected void doExecute(WorkflowExecutorContext context) {
        CallbackConfig decisionConfig = JsonUtil.parseObject(context.getNodeInfo(), CallbackConfig.class);

        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.set(HttpHeaders.CONTENT_TYPE, decisionConfig.getContentType());
        requestHeaders.set("secret", decisionConfig.getSecret());

        // TODO 拼接所有的任务结果值传递给下游节点
        List<JobTask> jobTasks = jobTaskMapper.selectList(new LambdaQueryWrapper<JobTask>()
                .select(JobTask::getResultMessage)
                .eq(JobTask::getTaskBatchId, context.getTaskBatchId()));

        Map<String, String> uriVariables = new HashMap<>();
        uriVariables.put("secret", decisionConfig.getSecret());
        restTemplate.exchange(decisionConfig.getWebhook(), HttpMethod.POST,
                new HttpEntity<>("", requestHeaders), Object.class, uriVariables);

        // TODO 保存批次
        // TODO 保存任务
        // TODO 保存日志
    }
}
