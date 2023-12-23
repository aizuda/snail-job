package com.aizuda.easy.retry.server.job.task.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author www.byteblogs.com
 * @date 2023-09-25 22:42:21
 * @since 2.4.0
 */
@Data
public class JobTaskPrepareDTO {

    private Long jobId;

    private String namespaceId;

    /**
     * 组名称
     */
    private String groupName;

    /**
     * 下次触发时间
     */
    private long nextTriggerAt;

    /**
     * 阻塞策略 1、丢弃 2、覆盖 3、并行
     */
    private Integer blockStrategy;

    /**
     * 任务类型
     */
    private Integer taskType;

    /**
     * 任务执行超时时间，单位秒
     */
    private Integer executorTimeout;

    private Long taskBatchId;

    private String clientId;

    /**
     * 任务执行时间
     */
    private Long executionAt;

    private boolean onlyTimeoutCheck;

    /**
     * 触发类似 1、auto 2、manual
     */
    private Integer triggerType;

    /**
     * 工作流任务批次id
     */
    private Long workflowTaskBatchId;

    /**
     * 工作流节点id
     */
    private Long workflowNodeId;

    /**
     * 工作流父节点id
     */
    private Long parentWorkflowNodeId;


}
