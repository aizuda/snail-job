package com.aizuda.snailjob.server.job.task.dto;

import lombok.Data;


/**
 * @author xiaowoniu
 * @date 2023-12-24 23:00:24
 * @since 2.6.0
 */
@Data
public class CompleteJobBatchDTO {

    private Long workflowNodeId;
    private Long workflowTaskBatchId;
    private Long taskBatchId;
    private Integer jobOperationReason;
    private Object result;

}
