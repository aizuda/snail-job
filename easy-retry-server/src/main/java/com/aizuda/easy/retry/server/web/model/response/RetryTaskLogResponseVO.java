package com.aizuda.easy.retry.server.web.model.response;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author: www.byteblogs.com
 * @date : 2022-02-28 09:09
 */
@Data
public class RetryTaskLogResponseVO {

    private Long id;

    private String uniqueId;

    private String groupName;

    private String sceneName;

    private String idempotentId;

    private String bizNo;

    private String executorName;

    private String argsStr;

    private String extAttrs;

    private LocalDateTime nextTriggerAt;

    private Integer retryStatus;

    private Integer taskType;

    private LocalDateTime createDt;

}
