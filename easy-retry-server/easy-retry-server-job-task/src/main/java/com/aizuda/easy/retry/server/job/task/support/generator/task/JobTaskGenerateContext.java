package com.aizuda.easy.retry.server.job.task.support.generator.task;

import lombok.Data;

/**
 * @author www.byteblogs.com
 * @date 2023-10-02 13:02:57
 * @since 2.4.0
 */
@Data
public class JobTaskGenerateContext {
    private Long taskBatchId;
    private String groupName;
    private Long jobId;
}
