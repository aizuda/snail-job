package com.aizuda.snailjob.server.common.vo;

import com.aizuda.snailjob.server.common.vo.base.BaseQueryVO;
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
