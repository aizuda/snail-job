package com.aizuda.snailjob.server.service.dto;

import lombok.Builder;
import lombok.Data;

import java.util.Date;

/**
 * <p>
 *
 * </p>
 *
 * @author opensnail
 * @date 2025-07-05
 */
@Data
@Builder
public class CalculateNextTriggerAtDTO {
    private Long id;
    private Integer oldResident;
    private Integer newResident;
    private Integer triggerType;
    private String triggerInterval;
//    private Date nextTriggerAt;
}
