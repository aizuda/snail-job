package com.aizuda.easy.retry.client.common.dto;

import lombok.Data;

/**
 * @author: www.byteblogs.com
 * @date : 2023-09-27 09:40
 */
@Data
public class JobContext {

    private Long jobId;

    private Long taskBatchId;

    private Long workflowTaskBatchId;

    private Long workflowNodeId;

    private Long taskId;

    private String namespaceId;

    private String groupName;

    private String executorInfo;

    private Integer taskType;

    private Integer parallelNum;

    private Integer shardingTotal;

    private Integer shardingIndex;

    private Integer executorTimeout;

    private String argsStr;
}
