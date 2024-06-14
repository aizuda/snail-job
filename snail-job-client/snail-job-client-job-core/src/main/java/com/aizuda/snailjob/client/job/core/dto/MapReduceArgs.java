package com.aizuda.snailjob.client.job.core.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * @author: opensnail
 * @date : 2024-06-13
 * @since : sj_1.1.0
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class MapReduceArgs extends JobArgs {

    private String mapName;

    private List<MapArgs> mapArgsList;
}
