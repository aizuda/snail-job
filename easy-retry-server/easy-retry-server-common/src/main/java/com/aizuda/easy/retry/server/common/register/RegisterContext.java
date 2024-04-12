package com.aizuda.easy.retry.server.common.register;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author opensnail
 * @date 2023-06-07
 * @since 2.0
 */
@Data
public class RegisterContext {

    private String namespaceId;

    private String groupName;

    private String hostId;

    private String hostIp;

    private Integer hostPort;

    private LocalDateTime expireAt;

    private Integer nodeType;

    private String contextPath;

    private String extAttrs;

    private String uri;

}
