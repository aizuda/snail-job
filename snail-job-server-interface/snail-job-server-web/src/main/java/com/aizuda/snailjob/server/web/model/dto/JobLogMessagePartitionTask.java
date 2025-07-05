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
public class JobLogMessagePartitionTask extends PartitionTask {

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
     * 日志数量
     */
    private Integer logNum;

    /**
     * 调度信息
     */
    private String message;

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
