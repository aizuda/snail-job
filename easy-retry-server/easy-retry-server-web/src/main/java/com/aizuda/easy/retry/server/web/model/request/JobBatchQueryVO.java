package com.aizuda.easy.retry.server.web.model.request;

import com.aizuda.easy.retry.server.web.model.base.BaseQueryVO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author www.byteblogs.com
 * @date 2023-10-11 22:28:07
 * @since 2.4.0
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class JobBatchQueryVO extends BaseQueryVO {
    private Long jobId;
    private String jobName;
    private Integer taskBatchStatus;
    private String groupName;
}
