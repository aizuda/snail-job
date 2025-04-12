package com.aizuda.snailjob.server.web.model.request;

import com.aizuda.snailjob.server.common.vo.base.BaseQueryVO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author xiaowoniu
 * @date 2023-12-23 17:49:59
 * @since 2.6.0
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class WorkflowBatchQueryVO extends BaseQueryVO {
    private String groupName;
    private Long workflowId;
    private Integer taskBatchStatus;
}
