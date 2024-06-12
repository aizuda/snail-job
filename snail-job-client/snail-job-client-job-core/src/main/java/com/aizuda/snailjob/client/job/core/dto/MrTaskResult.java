package com.aizuda.snailjob.client.job.core.dto;

import lombok.Data;

/**
 * Task执行结果
 *
 * @author: opensnail
 * @date : 2024-06-12 13:59
 */
@Data
public class MrTaskResult {

    private String taskId;

    private boolean success;

    private String result;
}
