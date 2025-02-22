package com.aizuda.snailjob.server.retry.task.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 *
 * </p>
 *
 * @author opensnail
 * @date 2025-02-11
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class TaskStopJobDTO extends BaseDTO {

    /**
     * 操作原因
     */
    private Integer operationReason;

    /**
     * 若是失败补充失败信息
     */
    private String message;

    /**
     * 是否需要变更任务状态
     */
    private boolean needUpdateTaskStatus;
}
