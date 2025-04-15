package com.aizuda.snailjob.template.datasource.persistence.dataobject.log;

import com.aizuda.snailjob.template.datasource.persistence.dataobject.common.PageQueryDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class RetryTaskLogMessageQueryDO extends PageQueryDO {
    private String groupName;

    private Long retryTaskId;

    private Long startId;

    private Integer fromIndex;

    private String sid;
}