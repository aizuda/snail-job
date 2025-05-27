package com.aizuda.snailjob.server.web.model.response;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * @author opensnail
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

    private LocalDateTime createDt;

    private LocalDateTime updateDt;

    private String extAttrs;

    private Set<Integer> consumerBuckets;

    private String labels;
}
