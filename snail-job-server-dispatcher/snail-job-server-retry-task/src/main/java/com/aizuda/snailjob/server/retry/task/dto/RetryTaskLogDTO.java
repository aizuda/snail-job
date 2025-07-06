package com.aizuda.snailjob.server.retry.task.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 日志上下文模型
 *
 * @author: opensnail
 * @date : 2023-06-16 14:14
 * @since 2.0.0
 */
@Data
public class RetryTaskLogDTO {

    /**
     * 命名空间
     */
    private String namespaceId;

    /**
     * 组名称
     */
    private String groupName;

    /**
     * 重试任务id
     */
    private Long retryTaskId;

    /**
     * 重试信息Id
     */
    private Long retryId;

    /**
     * 异常信息
     */
    private String message;

    /**
     * 重试状态
     */
    private Integer retryStatus;

    /**
     * 触发时间
     */
    private LocalDateTime triggerTime;

    /**
     * 客户端信息
     */
    private String clientInfo;

    /**
     * 真实上报时间
     */
    private Long realTime;

}
