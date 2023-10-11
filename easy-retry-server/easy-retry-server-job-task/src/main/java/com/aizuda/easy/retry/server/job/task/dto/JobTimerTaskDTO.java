package com.aizuda.easy.retry.server.job.task.dto;

import lombok.Data;

/**
 * @author www.byteblogs.com
 * @date 2023-09-30 23:19:39
 * @since 2.4.0
 */
@Data
public class JobTimerTaskDTO {

    private Long taskBatchId;
    private String groupName;
    private Long jobId;
}
