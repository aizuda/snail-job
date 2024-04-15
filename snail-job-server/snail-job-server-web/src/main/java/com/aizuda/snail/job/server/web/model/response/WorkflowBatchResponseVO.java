package com.aizuda.snail.job.server.web.model.response;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author xiaowoniu
 * @date 2023-12-23 17:50:37
 * @since 2.6.0
 */
@Data
public class WorkflowBatchResponseVO {

    private Long id;

    /**
     * 组名称
     */
    private String groupName;

    /**
     * 工作流任务id
     */
    private Long workflowId;

    /**
     * 工作流任务名称
     */
    private String workflowName;

    /**
     * 任务批次状态 0、失败 1、成功
     */
    private Integer taskBatchStatus;

    /**
     * 操作原因
     */
    private Integer operationReason;

    /**
     * 任务执行时间
     */
    private LocalDateTime executionAt;

    /**
     * 创建时间
     */
    private LocalDateTime createDt;

}
