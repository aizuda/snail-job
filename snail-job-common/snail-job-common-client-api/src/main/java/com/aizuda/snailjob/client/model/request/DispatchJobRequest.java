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

    @NotBlank(message = "namespaceId 不能为空")
    private String namespaceId;

    @NotNull(message = "jobId 不能为空")
    private Long jobId;

    @NotNull(message = "taskBatchId 不能为空")
    private Long taskBatchId;

    @NotNull(message = "taskId 不能为空")
    private Long taskId;

    @NotNull(message = "taskType 不能为空")
    private Integer taskType;

    @NotBlank(message = "group 不能为空")
    private String groupName;

    @NotNull(message = "parallelNum 不能为空")
    private Integer parallelNum;

    @NotNull(message = "executorType 不能为空")
    private Integer executorType;

    @NotBlank(message = "executorInfo 不能为空")
    private String executorInfo;

    @NotNull(message = "executorTimeout 不能为空")
    private Integer executorTimeout;

    /**
     * 任务名称
     */
    private String taskName;

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
    private boolean isRetry;

}
