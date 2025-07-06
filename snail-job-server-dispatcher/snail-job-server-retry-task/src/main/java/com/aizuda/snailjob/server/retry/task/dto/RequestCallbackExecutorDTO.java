package com.aizuda.snailjob.server.retry.task.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 *
 * </p>
 *
 * @author opensnail
 * @date 2025-01-26
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class RequestCallbackExecutorDTO extends BaseDTO {

    private String clientId;

    private Integer routeKey;

    private Integer executorTimeout;

    private String argsStr;

    private String executorName;

    private Long parentId;

    private String serializerName;

    private String labels;
}
