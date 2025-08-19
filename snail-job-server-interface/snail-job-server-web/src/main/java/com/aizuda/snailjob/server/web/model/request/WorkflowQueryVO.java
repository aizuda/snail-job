package com.aizuda.snailjob.server.web.model.request;

import com.aizuda.snailjob.server.service.dto.base.BaseQueryDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author: xiaowoniu
 * @date : 2023-12-15 12:09
 * @since : 2.6.0
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class WorkflowQueryVO extends BaseQueryDTO {

    private String groupName;

    private String workflowName;

    private Integer workflowStatus;

    private Long ownerId;
}
