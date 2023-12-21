package com.aizuda.easy.retry.server.job.task.dto;

import com.aizuda.easy.retry.server.common.dto.PartitionTask;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author xiaowoniu
 * @date 2023-12-21 21:38:52
 * @since 2.6.0
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class WorkflowPartitionTaskDTO extends PartitionTask {

    /**
     * 命名空间id
     */
    private String namespaceId;

    /**
     * 组名称
     */
    private String groupName;

    /**
     * 触发类型
     */
    private Integer triggerType;

    /**
     * 触发间隔
     */
    private String triggerInterval;

    /**
     * 执行超时时间
     */
    private Integer executorTimeout;

    /**
     * 任务执行时间
     */
    private Long nextTriggerAt;

    /**
     * 流程信息
     */
    private String flowInfo;

}
