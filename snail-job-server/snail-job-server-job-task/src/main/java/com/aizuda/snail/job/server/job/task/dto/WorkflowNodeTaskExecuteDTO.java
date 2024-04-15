package com.aizuda.snail.job.server.job.task.dto;

import lombok.Data;

/**
 * @author: xiaowoniu
 * @date : 2023-12-22
 * @since : 2.6.0
 */
@Data
public class WorkflowNodeTaskExecuteDTO {

    /**
     * 工作流任务批次id
     */
    private Long workflowTaskBatchId;
    /**
     * 执行策略 1、auto 2、manual 3、workflow
     */
    private Integer taskExecutorScene;

    private Long parentId;

    /**
     * 调度任务id
     */
    private Long taskBatchId;
}
