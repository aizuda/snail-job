package com.aizuda.snailjob.server.model.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author xiaowoniu
 * @date 2024-03-20 23:08:26
 * @since 3.2.0
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class RetryLogTaskDTO extends LogTaskDTO {

    private Long retryTaskId;

    private Long retryId;

    private String clientInfo;
}
