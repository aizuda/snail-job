package com.aizuda.snailjob.server.common.dto;

import lombok.Data;

/**
 * @author xiaowoniu
 * @date 2023-12-03 10:02:43
 * @since 2.5.0
 */
@Data
public class NotifyConfigInfo {

    private Long id;

    private String namespaceId;

    private String groupName;

    // job告警时使用
    private Long jobId;

    // retry告警时使用
    private String sceneName;

    private Integer notifyStatus;

    private Integer notifyType;

    private String notifyAttribute;

    private Integer notifyThreshold;

    private Integer notifyScene;

    private Integer rateLimiterStatus;

    private Integer rateLimiterThreshold;

}
