package com.aizuda.snailjob.server.web.model.dto;

import com.aizuda.snailjob.server.common.dto.PartitionTask;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * <p>
 *
 * </p>
 *
 * @author opensnail
 * @date 2025-03-30
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class LogMessagePartitionTask extends PartitionTask {

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
