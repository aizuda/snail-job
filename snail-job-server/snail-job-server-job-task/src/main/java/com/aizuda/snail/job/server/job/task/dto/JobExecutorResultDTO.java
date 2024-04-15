package com.aizuda.snail.job.server.job.task.dto;

import lombok.Data;

/**
 * @author opensnail
 * @date 2023-10-05 17:18:38
 * @since 2.4.0
 */
@Data
public class JobExecutorResultDTO {

    private Long jobId;

    private Long taskBatchId;

    /**
     * 工作流任务批次id
     */
    private Long workflowTaskBatchId;

    private Long workflowNodeId;

    private Long taskId;

    /**
     * 命名空间
     */
    private String namespaceId;

    private String groupName;

    private Integer taskStatus;

    private String message;

    private Integer taskType;

    private Object result;

    private Integer jobOperationReason;


}
