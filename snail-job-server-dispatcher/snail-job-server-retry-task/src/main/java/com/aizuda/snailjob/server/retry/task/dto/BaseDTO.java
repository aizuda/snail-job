package com.aizuda.snailjob.server.retry.task.dto;

import lombok.Data;

/**
 * <p>
 *
 * </p>
 *
 * @author opensnail
 * @date 2025-02-02
 */
@Data
public class BaseDTO {

    private String namespaceId;

    private String groupName;

    private String sceneName;

    private Long retryId;

    private Integer taskType;

    private Long retryTaskId;

}
