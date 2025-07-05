package com.aizuda.snailjob.server.web.model.response;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Set;

@Data
public class NotifyConfigResponseVO implements Serializable {

    private Long id;

    private String groupName;

    /**
     * 业务id (scene_name或job_id或workflow_id)
     */
    private String businessId;

    private String businessName;

    /**
     * 任务类型 1、重试任务 2、回调任务、 3、JOB任务 4、WORKFLOW任务
     */
    private Integer systemTaskType;

    private Integer notifyStatus;

    private String notifyName;

    private Set<Long> recipientIds;

    private Set<String> recipientNames;

    private Integer notifyThreshold;

    private Integer notifyScene;

    private Integer rateLimiterStatus;

    private Integer rateLimiterThreshold;

    private String description;

    private LocalDateTime createDt;

    private LocalDateTime updateDt;

    @Serial
    private static final long serialVersionUID = 1L;


}
