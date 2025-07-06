package com.aizuda.snailjob.server.common.dto;

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

    /**
     * 系统版本
     */
    private String systemVersion;

    /**
     * 执行器类型
     */
    private String executorType;
}
