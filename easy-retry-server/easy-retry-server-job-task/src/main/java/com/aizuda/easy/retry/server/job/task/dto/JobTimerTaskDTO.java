package com.aizuda.easy.retry.server.job.task.dto;

import lombok.Data;

/**
 * @author opensnail
 * @date 2023-09-30 23:19:39
 * @since 2.4.0
 */
@Data
public class JobTimerTaskDTO {

    private Long taskBatchId;
    private Long jobId;
    /**
     * 工作流任务批次id
     */
    private Long workflowTaskBatchId;

    private Long workflowNodeId;
    /**
     * 执行策略 1、auto 2、manual 3、workflow
     */
    private Integer taskExecutorScene;
}
