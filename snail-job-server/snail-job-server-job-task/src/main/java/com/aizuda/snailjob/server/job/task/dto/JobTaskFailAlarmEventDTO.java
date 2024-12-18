package com.aizuda.snailjob.server.job.task.dto;

import lombok.Builder;
import lombok.Data;

/**
 * @author zhengweilin
 * @version 1.0.0
 * @date 2024/12/12
 */
@Data
@Builder
public class JobTaskFailAlarmEventDTO {

    /**
     * 任务批次id
     */
    private Long jobTaskBatchId;

    /**
     * 通知场景
     */
    private Integer notifyScene;

    /**
     * 原因
     */
    private String reason;

}
