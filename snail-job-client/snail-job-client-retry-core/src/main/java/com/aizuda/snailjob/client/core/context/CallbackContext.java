package com.aizuda.snailjob.client.core.context;

import com.aizuda.snailjob.client.core.retryer.RetryerInfo;
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
public final class CallbackContext {

    private String namespaceId;
    private String groupName;
    private String scene;
    private Long retryTaskId;
    private Long retryId;
    private Integer retryStatus;
    private Object[] deSerialize;
    private RetryerInfo retryerInfo;
}
