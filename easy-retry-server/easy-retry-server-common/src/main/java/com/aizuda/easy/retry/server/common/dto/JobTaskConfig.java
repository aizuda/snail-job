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
    @NotNull(message = "任务ID不能为空")
    private Long jobId;

}
