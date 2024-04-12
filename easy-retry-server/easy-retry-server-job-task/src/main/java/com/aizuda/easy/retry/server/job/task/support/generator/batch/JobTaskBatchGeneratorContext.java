package com.aizuda.easy.retry.server.job.task.support.generator.batch;

import lombok.Data;

/**
 * @author opensnail
 * @date 2023-10-02 13:12:48
 * @since 2.4.0
 */
@Data
public class JobTaskBatchGeneratorContext {


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
     * 下次触发时间
     */
    private Long nextTriggerAt;

    /**
     * 操作原因
     */
    private Integer operationReason;

    /**
     * 任务批次状态
     */
    private Integer taskBatchStatus;

    /**
     * 执行策略 1、auto 2、manual 3、workflow
     */
    private Integer taskExecutorScene;

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


}
