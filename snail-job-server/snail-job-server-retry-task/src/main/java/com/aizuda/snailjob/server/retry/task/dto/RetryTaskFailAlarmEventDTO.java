package com.aizuda.snailjob.server.retry.task.dto;

import lombok.Builder;
import lombok.Data;

/**
 * @author zhengweilin
 * @version 1.0.0
 * @date 2024/12/12
 */
@Data
@Builder
public class RetryTaskFailAlarmEventDTO {

    private Long retryTaskId;

    private String reason;
}
