package com.aizuda.easy.retry.client.model.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @author: www.byteblogs.com
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

    private String argsStr;

    private Integer shardingTotal;

    private Integer shardingIndex;

    private Long workflowTaskBatchId;

    private Long workflowNodeId;

    private Integer retryCount;

}
