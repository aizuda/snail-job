package com.aizuda.snail.job.client.job.core.dto;

import lombok.Data;

/**
 * @author: opensnail
 * @date : 2023-10-18 16:53
 * @since : 2.4.0
 */
@Data
public class JobArgs {

    private String argsStr;

    private String executorInfo;

    private Long taskBatchId;
}
