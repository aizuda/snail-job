package com.aizuda.snailjob.server.job.task.dto;

import lombok.Data;

/**
 * @author: shuguang.zhang
 * @date : 2024-06-12
 */
@Data
public class ReduceTaskDTO {

    private Long workflowNodeId;
    private Long workflowTaskBatchId;
    private Long taskBatchId;
    private Long jobId;
}
