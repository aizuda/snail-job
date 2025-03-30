package com.aizuda.snailjob.template.datasource.persistence.dataobject.log;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * <p>
 *
 * </p>
 *
 * @author opensnail
 * @date 2025-03-29
 */
@Data
public class RetryTaskLogMessageDO {

    /**
     * 主键
     */
    private Long id;

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
     * 日志数量
     */
    private Integer logNum;

    /**
     * 真实上报时间
     */
    private Long realTime;

    /**
     * 创建时间
     */
    private LocalDateTime createDt;

    /**
     * 创建时间
     */
    private LocalDateTime updateDt;
}
