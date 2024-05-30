package com.aizuda.snailjob.server.web.model.request;

import lombok.Data;

import java.util.Set;

/**
 * @author: opensnail
 * @date : 2024-05-30
 * @since : sj_1.0.0
 */
@Data
public class ExportWorkflowVO {

    private Set<Long> workflowIds;

    private String groupName;

    private String workflowName;

    private Integer workflowStatus;
}
