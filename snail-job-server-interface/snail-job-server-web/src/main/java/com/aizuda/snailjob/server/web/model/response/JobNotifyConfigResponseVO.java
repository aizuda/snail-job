package com.aizuda.snailjob.server.web.model.response;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author: zuoJunLin
 * @date : 2023-12-02 11:22
 * @since : 2.5.0
 */
@Data
public class JobNotifyConfigResponseVO implements Serializable {

    private Long id;

    private String namespaceId;

    private String groupName;

    private Long jobId;

    private String jobName;

    private Integer notifyStatus;

    private String notifyName;

    private Integer notifyType;

    private String notifyAttribute;

    private Integer notifyThreshold;

    private Integer notifyScene;

    private Integer rateLimiterStatus;

    private Integer rateLimiterThreshold;

    private String description;

    private LocalDateTime createDt;

    private LocalDateTime updateDt;

    private static final long serialVersionUID = 1L;


}
