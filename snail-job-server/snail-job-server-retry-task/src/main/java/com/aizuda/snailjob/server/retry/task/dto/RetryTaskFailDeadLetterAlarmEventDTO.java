package com.aizuda.snailjob.server.retry.task.dto;

import lombok.Data;

/**
 * author: zhangshuguang
 * date: 2025-02-24
 */
@Data
public class RetryTaskFailDeadLetterAlarmEventDTO {

    private String namespaceId;

    private String groupName;

    private String sceneName;

    private String idempotentId;

    private String bizNo;

    private String executorName;

    private String argsStr;

    private String extAttrs;

}
