package com.aizuda.easy.retry.server.web.model.response;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author www.byteblogs.com
 * @date 2023-06-06
 * @since 2.0
 */
@Data
public class ServerNodeResponseVO {

    private Long id;

    private String groupName;

    private String hostId;

    private String hostIp;

    private Integer hostPort;

    private Integer nodeType;

    private String contextPath;

    private LocalDateTime createDt;

    private LocalDateTime updateDt;
}
