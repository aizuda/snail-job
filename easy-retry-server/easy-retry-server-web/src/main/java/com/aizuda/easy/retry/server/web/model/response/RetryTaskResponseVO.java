package com.aizuda.easy.retry.server.web.model.response;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author www.byteblogs.com
 * @date 2022-02-27
 * @since 2.0
 */
@Data
public class RetryTaskResponseVO {

    private Long id;

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

    private LocalDateTime createDt;

    private LocalDateTime updateDt;

}
