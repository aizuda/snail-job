package com.aizuda.snail.job.server.web.model.request;

import com.aizuda.snail.job.server.web.model.base.BaseQueryVO;
import lombok.Data;

/**
 * @author opensnail
 * @date 2022-02-27
 * @since 2.0
 */
@Data
public class RetryTaskQueryVO extends BaseQueryVO {

    private String groupName;

    private String sceneName;

    private String bizNo;

    private String idempotentId;

    private Integer retryStatus;

    private String uniqueId;
}
