package com.aizuda.easy.retry.server.job.task.dto;

import lombok.Data;

/**
 * @author www.byteblogs.com
 * @date 2023-12-22
 * @since 2.6.0
 */
@Data
public class WorkflowTimerTaskDTO {

    private Long workflowTaskBatchId;

    private Long workflowId;

    /**
     * 执行策略 1、auto 2、manual 3、workflow
     */
    private Integer executeStrategy;
}
