package com.aizuda.snailjob.server.retry.task.dto;

import lombok.Data;

/**
 * <p>
 *
 * </p>
 *
 * @author opensnail
 * @date 2025-01-26
 */
@Data
public class RetryTaskPrepareDTO {

    private String namespaceId;

    private String groupName;

    private String sceneName;

    private Long retryId;

    private Long retryTaskId;

    private Integer taskStatus;

    private Integer taskType;

    private Long nextTriggerAt;

    private Integer blockStrategy;

    private boolean onlyTimeoutCheck;

    private Integer executorTimeout;
}
