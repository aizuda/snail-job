package com.aizuda.snailjob.server.job.task.dto;

import lombok.Builder;
import lombok.Data;

/**
 * @author zhengweilin
 * @version 1.0.0
 * @date 2024/12/23
 */
@Data
@Builder
public class WorkflowTaskFailAlarmEventDTO {

    /**
     * 工作流任务批次id
     */
    private Long workflowTaskBatchId;

    /**
     * 通知场景
     */
    private Integer notifyScene;

    /**
     * 失败原因
     */
    private String reason;
}
