package com.aizuda.snailjob.server.retry.task.dto;

import lombok.Builder;
import lombok.Data;

/**
 * 重试任务失败告警
 */
@Data
@Builder
public class RetryTaskFailAlarmEventDTO {

    private Long retryTaskId;

    private String reason;

}
