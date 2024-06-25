package com.aizuda.snailjob.server.web.model.request;

import com.aizuda.snailjob.server.web.model.base.BaseQueryVO;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author: opensnail
 * @date : 2022-02-28 09:08
 */
@Data
public class RetryTaskLogQueryVO extends BaseQueryVO {

    private String groupName;

    private String sceneName;

    private String bizNo;

    private String idempotentId;

    private String uniqueId;

    private Integer retryStatus;

    private LocalDateTime beginDate;

    private LocalDateTime endDate;
}
