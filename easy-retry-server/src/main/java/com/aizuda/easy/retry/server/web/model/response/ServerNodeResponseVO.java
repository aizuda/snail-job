package com.aizuda.easy.retry.server.web.model.response;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * @author www.byteblogs.com
 * @date 2023-06-06
 * @since 2.0
 */
@Data
public class ServerNodeResponseVO {

    private String groupName;

    private String hostId;

    private String hostIp;

    private Integer hostPort;

    private Integer nodeType;

    private String contextPath;

    private LocalDateTime createDt;

    private LocalDateTime updateDt;

    private String extAttrs;

    private Set<String> consumerGroup;
}
