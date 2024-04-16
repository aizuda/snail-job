package com.aizuda.snailjob.template.datasource.persistence.dataobject;

import lombok.Data;

import java.util.List;

/**
 * @author opensnail
 * @date 2023-12-23 23:03:01
 * @since 2.6.0
 */
@Data
public class WorkflowBatchQueryDO {

    private List<String> groupNames;
    private Integer taskBatchStatus;
    private String workflowName;
    private Long workflowId;
    /**
     * 命名空间id
     */
    private String namespaceId;

}
