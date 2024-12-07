package com.aizuda.snailjob.server.common.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * @author xiaowoniu
 * @date 2023-12-03 10:15:37
 * @since 2.5.0
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class RetryAlarmInfo extends AlarmInfo {

    private String namespaceId;

    private String uniqueId;

    private String groupName;

    private String sceneName;

    private String idempotentId;

    private String bizNo;

    private String executorName;

    private String argsStr;

    private Integer retryCount;

    private LocalDateTime createDt;

    private String notifyIds;

}
