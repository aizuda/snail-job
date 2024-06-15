package com.aizuda.snailjob.client.job.core.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Task执行结果
 *
 * @author: opensnail
 * @date : 2024-06-12 13:59
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class MapArgs extends JobArgs {

    private String taskName;

}
