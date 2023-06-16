package com.aizuda.easy.retry.server.web.model.response;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author: www.byteblogs.com
 * @date : 2022-02-28 09:36
 */
@Data
public class RetryDeadLetterResponseVO {

    private Long id;

    private String groupName;

    private String sceneName;

    private String idempotentId;

    private String bizNo;

    private String executorName;

    private String argsStr;

    private String extAttrs;

    private Integer taskType;

    private String uniqueId;

    private LocalDateTime createDt;

}
