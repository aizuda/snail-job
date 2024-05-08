package com.aizuda.snailjob.template.datasource.persistence.dataobject;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author xiaowoniu
 * @date 2023-12-23 18:01:52
 * @since 2.6.0
 */
@Data
public class WorkflowBatchResponseDO {

    private Long id;

    /**
     * 命名空间id
     */
    private String namespaceId;

    /**
     * 组名称
     */
    private String groupName;

    /**
     * 工作流任务id
     */
    private Long workflowId;

    /**
     * 工作流任务名称
     */
    private String workflowName;

    /**
     * 任务批次状态 0、失败 1、成功
     */
    private Integer taskBatchStatus;

    /**
     * 操作原因
     */
    private Integer operationReason;

    /**
     * 任务执行时间
     */
    private Long executionAt;

    /**
     * 创建时间
     */
    private LocalDateTime createDt;
}
