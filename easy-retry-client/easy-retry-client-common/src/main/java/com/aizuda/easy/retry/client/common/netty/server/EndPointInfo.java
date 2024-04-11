package com.aizuda.easy.retry.client.common.netty.server;

import com.aizuda.easy.retry.client.common.netty.RequestMethod;
import lombok.Builder;
import lombok.Data;

import java.lang.reflect.Method;

/**
 * @author opensnail
 * @date 2024-04-11 22:35:32
 * @since 3.3.0
 */
@Builder
@Data
public class EndPointInfo {

    private final String executorName;
    private final Method method;
    private final Object executor;
    private final RequestMethod requestMethod;
    private final String path;
}
