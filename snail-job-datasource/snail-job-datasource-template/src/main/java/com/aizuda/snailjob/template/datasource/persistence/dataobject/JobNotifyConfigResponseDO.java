package com.aizuda.snailjob.template.datasource.persistence.dataobject;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author zuoJunLin
 * @date 2023-12-02 23:03:01
 * @since 2.4.0
 */
@Data
public class JobNotifyConfigResponseDO {

    private Long id;

    private String namespaceId;

    private String groupName;

    private Long jobId;

    private String jobName;

    private Integer notifyStatus;

    private Integer notifyType;

    private String notifyAttribute;

    private Integer notifyThreshold;

    private Integer notifyScene;

    private Integer rateLimiterStatus;

    private Integer rateLimiterThreshold;

    private String description;

    private LocalDateTime createDt;

    private LocalDateTime updateDt;

}
