package com.aizuda.snailjob.client.core.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RetryDTO {
    private Long id;

    private String namespaceId;

    private String groupName;

    private String sceneName;

    private String idempotentId;

    private String bizNo;

    private String argsStr;

    private String extAttrs;

    private String executorName;

    /**
     * 下次触发时间
     */
    private LocalDateTime nextTriggerAt;

    private Integer retryCount;

    private Integer retryStatus;

    private Integer taskType;

    private Long parentId;

    private Integer bucketIndex;

    private Long deleted;
}
