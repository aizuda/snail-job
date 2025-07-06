package com.aizuda.snailjob.server.retry.task.dto;

import lombok.Data;

import java.util.List;
import java.util.Set;

/**
 * @author opensnail
 * @date 2025-01-11
 * @since 1.3.0-beta1.1
 */
@Data
public class NotifyConfigDTO {

    private Long id;

    private Set<Long> recipientIds;

    private Integer notifyThreshold;

    private Integer notifyScene;

    private Integer rateLimiterStatus;

    private Integer rateLimiterThreshold;

    private List<RecipientInfo> recipientInfos;

    @Data
    public static class RecipientInfo {

        private Integer notifyType;

        private String notifyAttribute;
    }
}
