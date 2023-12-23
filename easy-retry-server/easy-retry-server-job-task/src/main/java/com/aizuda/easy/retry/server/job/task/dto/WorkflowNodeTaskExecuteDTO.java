package com.aizuda.easy.retry.server.job.task.dto;

import lombok.Data;

/**
 * @author: xiaowoniu
 * @date : 2023-12-22
 * @since : 2.6.0
 */
@Data
public class WorkflowNodeTaskExecuteDTO {

    /**
     * 工作流id
     */
    private Long workflowId;

    /**
     * 工作流任务批次id
     */
    private Long workflowTaskBatchId;
    /**
     * 触发类似 1、auto 2、manual
     */
    private Integer triggerType;

    private Long parentId;

}
