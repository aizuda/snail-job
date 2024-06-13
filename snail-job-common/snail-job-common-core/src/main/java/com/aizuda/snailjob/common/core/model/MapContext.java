package com.aizuda.snailjob.common.core.model;

import lombok.Data;

import java.util.List;

/**
 * @author: opensnail
 * @date : 2024-06-13
 * @since : sj_1.1.0
 */
@Data
public final class MapContext {

    /**
     * Map集合列表
     */
    private List<Object> taskList;

    /**
     * Map名称
     */
    private String mapName;

    private Long jobId;

    private Long taskBatchId;

    private Long taskId;
}
