package com.aizuda.easy.retry.server.web.model.request;

import lombok.Data;

/**
 * @author xiaowoniu
 * @date 2023-11-22 22:42:26
 * @since 2.5.0
 */
@Data
public final class UserSessionVO {

    private Long id;

    private String username;

    private Integer role;

    private String namespaceId;

}
