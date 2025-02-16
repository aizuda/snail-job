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
}
