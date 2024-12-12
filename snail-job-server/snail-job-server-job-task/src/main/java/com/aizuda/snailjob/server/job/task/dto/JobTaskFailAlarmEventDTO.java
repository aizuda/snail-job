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

    private Long jobTaskBatchId;

    private String reason;
}
