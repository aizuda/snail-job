package com.aizuda.easy.retry.server.web.model.response;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author: www.byteblogs.com
 * @date : 2023-10-12 11:22
 * @since : 2.4.0
 */
@Data
public class JobLogResponseVO {

    private Long id;

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
     * 创建时间
     */
    private LocalDateTime createDt;

    /**
     * 客户端信息
     */
    private String clientAddress;

    /**
     * 调度信息
     */
    private String message;

}
