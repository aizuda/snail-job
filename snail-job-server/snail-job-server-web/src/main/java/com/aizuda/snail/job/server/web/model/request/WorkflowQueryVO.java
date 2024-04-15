package com.aizuda.snail.job.server.web.model.request;

import com.aizuda.snail.job.server.web.model.base.BaseQueryVO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author: xiaowoniu
 * @date : 2023-12-15 12:09
 * @since : 2.6.0
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class WorkflowQueryVO extends BaseQueryVO {

    private String groupName;

    private String workflowName;

    private Integer workflowStatus;
}
