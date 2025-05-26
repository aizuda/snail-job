package com.aizuda.snailjob.server.common.dto;

import com.aizuda.snailjob.server.common.rpc.client.SnailJobRetryListener;
import com.github.rholder.retry.RetryListener;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * <p>
 *
 * </p>
 *
 * @author opensnail
 * @date 2025-05-24
 */
@Data
@Builder
@AllArgsConstructor
public class GrpcClientInvokeConfig {
    private String groupName;
    private InstanceLiveInfo instanceLiveInfo;
    private boolean failRetry;
    private int retryTimes;
    private int retryInterval;
    private SnailJobRetryListener retryListener;
    private Integer routeKey;
    private String allocKey;
    private boolean failover;
    private Integer executorTimeout;
    private String namespaceId;
    private boolean async; // 默认 false，可忽略设置
}
