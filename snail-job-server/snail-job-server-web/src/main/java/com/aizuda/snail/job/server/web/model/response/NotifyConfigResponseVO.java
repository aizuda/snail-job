package com.aizuda.snail.job.server.web.model.response;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class NotifyConfigResponseVO implements Serializable {

    private Long id;

    private String groupName;

    private String sceneName;

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
