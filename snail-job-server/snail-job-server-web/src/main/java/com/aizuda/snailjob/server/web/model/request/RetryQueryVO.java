package com.aizuda.snailjob.server.web.model.request;

import com.aizuda.snailjob.server.common.vo.base.BaseQueryVO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author opensnail
 * @date 2022-02-27
 * @since 2.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class RetryQueryVO extends BaseQueryVO {

    private String groupName;

    private String sceneName;

    private String bizNo;

    private String idempotentId;

    private Integer retryStatus;

    private Long retryId;
}
