package com.aizuda.snailjob.server.service.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RetryResponseDTO {

    private Long id;

    private String namespaceId;

    private String groupName;

    private String sceneName;

    private String idempotentId;

    private String bizNo;

    private String argsStr;

    private String extAttrs;

    private String executorName;

    private String serializerName;

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

    private LocalDateTime createDt;

    private LocalDateTime updateDt;
}
