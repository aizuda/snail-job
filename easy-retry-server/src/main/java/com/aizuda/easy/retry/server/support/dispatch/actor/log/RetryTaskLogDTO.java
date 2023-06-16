package com.aizuda.easy.retry.server.support.dispatch.actor.log;

import lombok.Data;

/**
 * 日志上下文模型
 *
 * @author: www.byteblogs.com
 * @date : 2023-06-16 14:14
 * @since 2.0.0
 */
@Data
public class RetryTaskLogDTO {

    /**
     * 组名称
     */
    private String groupName;

    /**
     * 同组下id唯一
     */
    private String uniqueId;

    /**
     * 异常信息
     */
    private String message;

    /**
     * 重试状态
     */
    private Integer retryStatus;

}
