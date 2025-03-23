package com.aizuda.snailjob.client.job.core.dto;

import lombok.Data;

/**
 * @author xiaowoniu
 * @date 2023-12-30 21:42:59
 * @since 2.6.0
 */
@Data
public class JobTaskConfig {

    /**
     * 任务ID
     */
    private Long jobId;

    /**
     * 任务名称
     */
    private String jobName;
}
