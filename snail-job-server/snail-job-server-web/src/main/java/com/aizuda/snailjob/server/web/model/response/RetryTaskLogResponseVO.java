package com.aizuda.snailjob.server.web.model.response;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author: opensnail
 * @date : 2022-02-28 09:09
 */
@Data
public class RetryTaskLogResponseVO {

    private Long id;

    private String groupName;

    private String sceneName;

    private Integer taskStatus;

    private Long retryId;


    private Integer taskType;

    private LocalDateTime createDt;

    private Integer operationReason;

    /**
     * 客户端ID
     */
    private String clientInfo;

}
