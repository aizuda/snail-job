package com.aizuda.snailjob.server.web.model.request;

import com.aizuda.snailjob.server.service.dto.base.BaseQueryDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author: opensnail
 * @date : 2022-02-28 09:08
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class RetryTaskLogMessageQueryVO extends BaseQueryDTO {

    private String groupName;
    private Long retryTaskId;
    private Long startRealTime;
    private String sid;

}
