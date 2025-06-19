package com.aizuda.snailjob.client.job.core.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * <p>
 *
 * </p>
 *
 * @since 1.6.0
 */
@Data
@AllArgsConstructor
public class PointInTimeDTO {

    /**
     * 指定时间 比如 2025-05-30 13:30:20
     */
    private Long time;

}
