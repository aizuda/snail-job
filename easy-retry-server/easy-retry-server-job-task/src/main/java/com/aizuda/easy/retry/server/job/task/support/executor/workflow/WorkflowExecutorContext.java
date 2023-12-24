package com.aizuda.easy.retry.server.job.task.support.executor.workflow;

import com.aizuda.easy.retry.template.datasource.persistence.po.Job;
import com.aizuda.easy.retry.template.datasource.persistence.po.JobTask;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * @author xiaowoniu
 * @date 2023-12-24
 * @since 2.6.0
 */
@Data
public class WorkflowExecutorContext {

    private String namespaceId;

    /**
     * 组名称
     */
    private String groupName;

    /**
     * 任务id
     */
    private Long jobId;

    /**
     * 工作流任务批次id
     */
    private Long workflowTaskBatchId;

    /**
     * 工作流节点id
     */
    private Long workflowNodeId;

    /**
     * 工作流父节点id
     */
    private Long parentWorkflowNodeId;

    /**
     * 任务属性
     */
    private Job job;

    private String expression;

    private Map<String, Object> expressionContext;

}
