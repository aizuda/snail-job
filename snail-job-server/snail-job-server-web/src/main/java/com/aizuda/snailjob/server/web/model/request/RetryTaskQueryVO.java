package com.aizuda.snailjob.server.web.model.request;

import com.aizuda.snailjob.server.web.model.base.BaseQueryVO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * @author: opensnail
 * @date : 2022-02-28 09:08
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class RetryTaskQueryVO extends BaseQueryVO {

    private String groupName;

    private String sceneName;

    private String bizNo;

    private String idempotentId;

    private Long retryId;

    private Integer taskStatus;

    private LocalDateTime beginDate;

    private LocalDateTime endDate;
}
