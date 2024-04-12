package com.aizuda.easy.retry.server.job.task.support.generator.batch;

import lombok.Data;

/**
 * @author opensnail
 * @date 2023-10-02 13:12:48
 * @since 2.4.0
 */
@Data
public class WorkflowTaskBatchGeneratorContext {

    private String namespaceId;

    /**
     * 组名称
     */
    private String groupName;

    /**
     * 工作流id
     */
    private Long workflowId;

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
     * 流程信息
     */
    private String flowInfo;


}
