package com.aizuda.snail.job.template.datasource.persistence.dataobject;

import lombok.Data;

import java.util.List;

/**
 * @author zuoJunLin
 * @date 2023-12-02 23:03:01
 * @since 2.4.0
 */
@Data
public class JobNotifyConfigQueryDO {

    private List<String> groupNames;

    private Long jobId;
    /**
     * 命名空间id
     */
    private String namespaceId;

}
