package com.aizuda.easy.retry.server.job.task.dto;

import lombok.Data;

/**
 * @author www.byteblogs.com
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
     * 触发类似 1、auto 2、manual
     */
    private Integer triggerType;
}
