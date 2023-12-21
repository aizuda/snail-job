package com.aizuda.easy.retry.server.job.task.dto;

import lombok.Data;

/**
 * @author xiaowoniu
 * @date 2023-12-21 22:25:11
 * @since 2.6.0
 */
@Data
public class WorkflowTaskPrepareDTO {

    private Long workflowId;

    /**
     * 触发类似 1、auto 2、manual
     */
    private Integer triggerType;

}
