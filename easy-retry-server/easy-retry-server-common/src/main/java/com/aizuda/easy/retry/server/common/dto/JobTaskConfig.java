package com.aizuda.easy.retry.server.common.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

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
