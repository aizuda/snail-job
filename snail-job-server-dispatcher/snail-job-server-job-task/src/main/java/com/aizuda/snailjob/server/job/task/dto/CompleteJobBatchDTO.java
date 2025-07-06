package com.aizuda.snailjob.server.job.task.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;


/**
 * @author xiaowoniu
 * @date 2023-12-24 23:00:24
 * @since 2.6.0
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class CompleteJobBatchDTO extends BaseDTO {

    private Long jobId;
    private Long workflowNodeId;
    private Long workflowTaskBatchId;
    private Long taskBatchId;
    private Integer jobOperationReason;
    private Object result;
    private String message;
    private Integer taskType;
    private Boolean retryStatus;

}
