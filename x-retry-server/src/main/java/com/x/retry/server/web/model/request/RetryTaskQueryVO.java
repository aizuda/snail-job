package com.x.retry.server.web.model.request;

import com.x.retry.server.web.model.base.BaseQueryVO;
import lombok.Data;

/**
 * @author www.byteblogs.com
 * @date 2022-02-27
 * @since 2.0
 */
@Data
public class RetryTaskQueryVO extends BaseQueryVO {

    private String groupName;

    private String sceneName;

    private String bizNo;

    private String bizId;

    private Integer retryStatus;
}
