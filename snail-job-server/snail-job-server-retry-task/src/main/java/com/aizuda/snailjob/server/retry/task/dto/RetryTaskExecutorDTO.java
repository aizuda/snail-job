package com.aizuda.snailjob.server.retry.task.dto;

import com.aizuda.snailjob.template.datasource.persistence.po.CreateUpdateDt;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RetryTaskExecutorDTO extends CreateUpdateDt {

    private Long id;

    private String namespaceId;

    private String uniqueId;

    private String groupName;

    private String sceneName;

    private String idempotentId;

    private String bizNo;

    private String argsStr;

    private String extAttrs;

    private String executorName;

    private LocalDateTime nextTriggerAt;

    private Integer retryCount;

    private Integer retryStatus;

    private Integer taskType;

    private Integer notifyScene;

    private String reason;

}
