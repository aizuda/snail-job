package com.aizuda.snailjob.server.retry.task.support.block;

import lombok.Data;

/**
 * @author: opensnail
 * @date : 2025-02-10
 * @since : sj_1.4.0
 */
@Data
public class BlockStrategyContext {

    private String namespaceId;

    private String groupName;

    private String sceneName;

    private Long retryId;

    private Long retryTaskId;

    private Integer taskStatus;

    private Integer taskType;

    private Long nextTriggerAt;

    private Integer blockStrategy;

    private Integer operationReason;
}
