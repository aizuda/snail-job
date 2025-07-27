package com.aizuda.snailjob.client.common.rpc.client.openapi;

import com.aizuda.snailjob.client.common.config.SnailJobProperties;
import lombok.Data;

/**
 * <p>
 *
 * </p>
 *
 * @author opensnail
 * @date 2025-07-05
 */
@Data
public final class SnailHttpClientConfig {
    /**
     * 默认可以不配置，不配置则取{@link SnailJobProperties.ServerConfig#getHost()}
     */
    private String host;
    /**
     * 默认是8080
     */
    private int port = 8080;
    /**
     * 是否是https协议
     */
    private boolean https;

    private String prefix = "snail-job";
}
