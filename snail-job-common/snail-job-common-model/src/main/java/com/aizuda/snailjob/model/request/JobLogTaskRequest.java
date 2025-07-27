package com.aizuda.snailjob.model.request;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author xiaowoniu
 * @date 2024-03-20 23:08:26
 * @since 3.2.0
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class JobLogTaskRequest extends LogTaskRequest {

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
