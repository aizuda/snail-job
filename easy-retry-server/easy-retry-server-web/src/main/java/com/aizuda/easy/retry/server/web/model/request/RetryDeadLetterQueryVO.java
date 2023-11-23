package com.aizuda.easy.retry.server.web.model.request;

import com.aizuda.easy.retry.server.web.model.base.BaseQueryVO;
import lombok.Data;

/**
 * @author: www.byteblogs.com
 * @date : 2022-02-28 09:45
 */
@Data
public class RetryDeadLetterQueryVO extends BaseQueryVO {
    private String groupName;
    private String sceneName;
    private String bizNo;
    private String idempotentId;
    private String uniqueId;
}
