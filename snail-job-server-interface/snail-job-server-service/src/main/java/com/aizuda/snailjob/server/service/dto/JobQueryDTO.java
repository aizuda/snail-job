package com.aizuda.snailjob.server.service.dto;

import lombok.Data;

import java.util.Set;

/**
 * <p>
 *
 * </p>
 *
 * @author opensnail
 * @date 2025-07-06
 */
@Data
public class JobQueryDTO {
    private Set<Long> jobIds;
}
