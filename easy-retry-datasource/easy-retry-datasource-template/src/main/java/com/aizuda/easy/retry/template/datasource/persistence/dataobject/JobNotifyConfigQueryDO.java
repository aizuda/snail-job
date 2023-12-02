package com.aizuda.easy.retry.template.datasource.persistence.dataobject;

import lombok.Data;

/**
 * @author zuoJunLin
 * @date 2023-12-02 23:03:01
 * @since 2.4.0
 */
@Data
public class JobNotifyConfigQueryDO {

    private String groupName;

    private Long jobId;
    /**
     * 命名空间id
     */
    private String namespaceId;

}
