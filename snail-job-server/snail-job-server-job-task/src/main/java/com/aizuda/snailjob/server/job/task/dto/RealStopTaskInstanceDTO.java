package com.aizuda.snailjob.server.job.task.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * @author: opensnail
 * @date : 2023-10-07 10:50
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class RealStopTaskInstanceDTO extends BaseDTO {

    /**
     * 下次触发时间
     */
    private LocalDateTime nextTriggerAt;
}
