package com.aizuda.snailjob.server.retry.task.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author zhengweilin
 * @version 1.0.0
 * @date 2024/12/23
 */
@Data
public class RetryTaskFailAlarmEventDTO {

    private Long id;

    private String namespaceId;

    private String uniqueId;

    private String groupName;

    private String sceneName;

    private String idempotentId;

    private String bizNo;

    private String argsStr;

    private String extAttrs;

    private String executorName;

    private LocalDateTime nextTriggerAt;

    private Integer retryCount;

    private Integer retryStatus;

    private Integer taskType;

    private Integer notifyScene;

    private String reason;

}
