package com.aizuda.snailjob.common.core.model;

import lombok.Data;

import java.util.List;

/**
 * @author: opensnail
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

    /**
     * 重试场景 auto、manual
     */
    private Integer retryScene;

    /**
     * 是否是重试流量
     */
    private boolean isRetry;

    /**
     * Map集合列表
     */
    private List<Object> taskList;

    /**
     * Map名称
     */
    private String mapName;
}
