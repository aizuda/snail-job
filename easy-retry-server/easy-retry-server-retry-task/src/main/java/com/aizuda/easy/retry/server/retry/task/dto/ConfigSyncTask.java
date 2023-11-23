package com.aizuda.easy.retry.server.retry.task.dto;

import lombok.Data;

/**
 * @author: xiaowoniu
 * @date : 2023-11-23 17:01
 * @since : 2.5.0
 */
@Data
public class ConfigSyncTask {

    private String groupName;
    private String namespaceId;
    private Integer currentVersion;
}
