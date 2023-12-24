package com.aizuda.easy.retry.server.job.task.dto;

import lombok.Data;

/**
 * @author xiaowoniu
 * @date 2023-12-21 22:25:11
 * @since 2.6.0
 */
@Data
public class WorkflowTaskPrepareDTO {

    private Long workflowTaskBatchId;

    private Long workflowId;

    /**
     * 触发类似 1、auto 2、manual
     */
    private Integer triggerType;

    /**
     * 工作流名称
     */
    private String workflowName;

    /**
     * 命名空间id
     */
    private String namespaceId;

    /**
     * 组名称
     */
    private String groupName;

    /**
     * 触发间隔
     */
    private String triggerInterval;

    /**
     * 执行超时时间
     */
    private Integer executorTimeout;

    /**
     * 工作流状态 0、关闭、1、开启
     */
    private Integer workflowStatus;

    /**
     * 流程信息
     */
    private String flowInfo;

    /**
     * 下次触发时间
     */
    private long nextTriggerAt;
}
