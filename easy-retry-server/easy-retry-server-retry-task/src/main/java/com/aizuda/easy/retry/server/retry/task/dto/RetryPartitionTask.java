package com.aizuda.easy.retry.server.retry.task.dto;

import com.aizuda.easy.retry.server.common.dto.PartitionTask;
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

    /**
     * 下次触发时间
     */
    private LocalDateTime nextTriggerAt;

    private Integer retryCount;

}
