package com.aizuda.easy.retry.server.job.task.dto;

import lombok.Data;

/**
 * @author: www.byteblogs.com
 * @date : 2023-09-26 15:39
 */
@Data
public class TaskExecuteDTO {

    private Long jobId;
    private Long taskBatchId;
    /**
     * 触发类似 1、auto 2、manual
     */
    private Integer triggerType;

}
