package com.aizuda.snail.job.server.web.model.request;

import com.aizuda.snail.job.server.web.model.base.BaseQueryVO;
import lombok.Data;

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
}
