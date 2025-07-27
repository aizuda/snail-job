package com.aizuda.snailjob.model.request;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author xiaowoniu
 * @date 2024-03-20 23:08:26
 * @since 3.2.0
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class RetryLogTaskRequest extends LogTaskRequest {

    private Long retryTaskId;

    private Long retryId;

    private String clientInfo;
}
