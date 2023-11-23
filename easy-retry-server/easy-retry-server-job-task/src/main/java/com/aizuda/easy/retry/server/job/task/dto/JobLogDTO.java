package com.aizuda.easy.retry.server.job.task.dto;

import lombok.Data;

/**
 * @author www.byteblogs.com
 * @date 2023-10-03 22:34:14
 * @since 2.4.0
 */
@Data
public class JobLogDTO {

    /**
     * 命名空间
     */
    private String namespaceId;

    /**
     * 组名称
     */
    private String groupName;

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


    /**
     * 调度信息
     */
    private String message;


}
