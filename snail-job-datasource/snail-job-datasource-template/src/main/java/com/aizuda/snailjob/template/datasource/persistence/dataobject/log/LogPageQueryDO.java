package com.aizuda.snailjob.template.datasource.persistence.dataobject.log;

import com.aizuda.snailjob.template.datasource.persistence.dataobject.common.PageQueryDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 *
 * </p>
 *
 * @author opensnail
 * @date 2025-03-29
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class LogPageQueryDO extends PageQueryDO {
    private Long startId;
    private Long jobId;
    private Long taskBatchId;
    private Long taskId;
    private Integer fromIndex;
}
