package com.aizuda.easy.retry.template.datasource.persistence.dataobject;

import lombok.Data;

/**
 * @author www.byteblogs.com
 * @date 2023-10-15 23:03:01
 * @since 2.4.0
 */
@Data
public class JobBatchQueryDO {

    private String groupName;
    private Integer taskBatchStatus;
    private String jobName;
    private Long jobId;
    /**
     * 命名空间id
     */
    private String namespaceId;

}
