package com.aizuda.snailjob.server.retry.task.dto;

import com.aizuda.snailjob.server.common.dto.PartitionTask;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * @author opensnail
 * @date 2023-10-25 22:23:24
 * @since 2.4.0
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class RetryPartitionTask extends PartitionTask {

    private String namespaceId;

    private String groupName;

    private String sceneName;

    private Integer taskType;

    /**
     * 下次触发时间
     */
    private Long nextTriggerAt;

    private Integer retryCount;

    private String idempotentId;

    private String bizNo;

    private String argsStr;

    private String extAttrs;

    private String executorName;

    private Integer retryStatus;

    private Long parentId;

    private Integer bucketIndex;

}
