package com.aizuda.snail.job.server.common.dto;

import lombok.Data;

/**
 * 服务端节点扩展参数
 *
 * @author: opensnail
 * @date : 2023-06-29 15:54
 */
@Data
public class ServerNodeExtAttrs {

    /**
     * web容器port
     */
    private Integer webPort;
}
