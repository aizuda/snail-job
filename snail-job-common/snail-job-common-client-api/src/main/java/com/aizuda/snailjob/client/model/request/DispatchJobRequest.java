package com.aizuda.snailjob.client.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * @author: opensnail
 * @date : 2023-09-26 15:10
 */
@Data
public class DispatchJobRequest {

    @NotBlank(message = "namespaceId cannot be null")
    private String namespaceId;

    @NotNull(message = "jobId cannot be null")
    private Long jobId;

    @NotNull(message = "taskBatchId cannot be null")
    private Long taskBatchId;

    @NotNull(message = "taskId cannot be null")
    private Long taskId;

    @NotNull(message = "taskType cannot be null")
    private Integer taskType;

    @NotBlank(message = "group cannot be null")
    private String groupName;

    @NotNull(message = "parallelNum cannot be null")
    private Integer parallelNum;

    @NotNull(message = "executorType cannot be null")
    private Integer executorType;

    @NotBlank(message = "executorInfo cannot be null")
    private String executorInfo;

    @NotNull(message = "executorTimeout cannot be null")
    private Integer executorTimeout;

    /**
     * 任务名称
     */
    private String taskName;

    private Integer mrStage;

    private String argsStr;

    private Integer shardingTotal;

    private Integer shardingIndex;

    private Long workflowTaskBatchId;

    private Long workflowNodeId;

    private Integer retryCount;

    /**
     * 重试场景 auto、manual
     */
    private Integer retryScene;

    /**
     * 是否是重试流量
     */
    @Deprecated
    private boolean isRetry;

    /**
     * 是否是重试流量
     */
    private Boolean retryStatus = Boolean.FALSE;

    /**
     * 工作流上下文
     */
    private String wfContext;
}
