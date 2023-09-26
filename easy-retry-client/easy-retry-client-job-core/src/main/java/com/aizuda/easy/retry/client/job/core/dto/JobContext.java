package com.aizuda.easy.retry.client.job.core.dto;

import lombok.Data;

/**
 * @author: www.byteblogs.com
 * @date : 2023-09-27 09:40
 */
@Data
public class JobContext {

    private Long jobId;

    private Long taskId;

    private String groupName;
}
