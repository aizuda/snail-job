package com.aizuda.easy.retry.server.web.model.request;

import com.aizuda.easy.retry.server.web.model.base.BaseQueryVO;
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
    private String workflowName;
    private Long workflowId;
    private Integer taskBatchStatus;
}
