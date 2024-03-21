package com.aizuda.easy.retry.client.job.core.log;

import com.aizuda.easy.retry.client.common.report.LogMeta;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author: xiaowoniu
 * @date : 2024-03-21
 * @since : 3.2.0
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class JobLogMeta extends LogMeta {

    /**
     * 任务信息id
     */
    private Long jobId;

    /**
     * 任务实例id
     */
    private Long taskBatchId;

    /**
     * 调度任务id
     */
    private Long taskId;
}
