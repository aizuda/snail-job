package com.aizuda.snail.job.server.job.task.dto;

import lombok.Data;

/**
 * @author opensnail
 * @date 2023-10-06 17:05:04
 * @since 2.4.0
 */
@Data
public class BaseDTO {

    private Long jobId;
    private Long taskBatchId;
    private String groupName;
    private String namespaceId;
    private Long taskId;
    private Integer taskType;
    private String clientId;
}
