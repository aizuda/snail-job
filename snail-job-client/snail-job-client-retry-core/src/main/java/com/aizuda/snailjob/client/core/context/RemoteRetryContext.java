package com.aizuda.snailjob.client.core.context;

import lombok.Data;

/**
 * <p>
 *
 * </p>
 *
 * @author opensnail
 * @date 2025-02-12
 */
@Data
public final class RemoteRetryContext {

    private String namespaceId;
    private String groupName;
    private String scene;
    private String argsStr;
    private String executorName;
    private Integer retryCount;
    private Long retryTaskId;
    private Long retryId;
    private Object[] deSerialize;
}
