package com.aizuda.snailjob.server.common.dto;

import lombok.Data;

import java.util.List;
import java.util.Set;

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

    // 业务id (scene_name或job_id或workflow_id)
    private String businessId;

    private Set<Long> recipientIds;

    // 任务类型 1、重试任务 2、回调任务、 3、JOB任务 4、WORKFLOW任务
    private Integer systemTaskType;

    private Integer notifyStatus;

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
