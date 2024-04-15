package com.aizuda.snail.job.server.web.model.response;

import lombok.Data;

import java.util.List;
import java.util.Set;

/**
 * @author: xiaowoniu
 * @date : 2023-11-23 14:01
 * @since : 2.5.0
 */
@Data
public class PermissionsResponseVO {

    private String groupName;
    private String namespaceId;
    private String namespaceName;
    private Set<String> groupNames;
}
