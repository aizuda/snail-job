package com.aizuda.easy.retry.client.common.log.report;

import lombok.Data;

/**
 * @author: xiaowoniu
 * @date : 2024-03-21
 * @since : 3.2.0
 */
@Data
public class LogMeta {
    /**
     * 命名空间
     */
    private String namespaceId;

    /**
     * 组名称
     */
    private String groupName;
}
