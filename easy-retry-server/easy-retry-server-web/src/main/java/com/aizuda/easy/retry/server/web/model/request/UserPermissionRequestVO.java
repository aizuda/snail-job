package com.aizuda.easy.retry.server.web.model.request;

import lombok.Data;

/**
 * @author: xiaowoniu
 * @date : 2023-11-22 09:15
 * @since : 2.5.0
 */
@Data
public class UserPermissionRequestVO {

    private String groupName;
    private String namespaceId;
}
