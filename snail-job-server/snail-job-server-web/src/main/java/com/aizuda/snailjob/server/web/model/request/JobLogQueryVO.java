package com.aizuda.snailjob.server.web.model.request;

import com.aizuda.snailjob.server.web.model.base.BaseQueryVO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author opensnail
 * @date 2023-10-11 22:28:07
 * @since 2.4.0
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class JobLogQueryVO extends BaseQueryVO {
    private Long startId;
    private Long jobId;
    private Long taskBatchId;
    private Long taskId;
    private Integer fromIndex;
}
