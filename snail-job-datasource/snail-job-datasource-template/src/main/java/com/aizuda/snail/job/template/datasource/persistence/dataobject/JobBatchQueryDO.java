package com.aizuda.snail.job.template.datasource.persistence.dataobject;

import lombok.Data;

import java.util.List;

/**
 * @author opensnail
 * @date 2023-10-15 23:03:01
 * @since 2.4.0
 */
@Data
public class JobBatchQueryDO {

    private List<String> groupNames;
    private Integer taskBatchStatus;
    private String jobName;
    private Long jobId;
    /**
     * 命名空间id
     */
    private String namespaceId;

}
