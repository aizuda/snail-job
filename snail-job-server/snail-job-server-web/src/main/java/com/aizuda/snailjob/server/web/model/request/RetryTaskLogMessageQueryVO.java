package com.aizuda.snailjob.server.web.model.request;

import com.aizuda.snailjob.server.web.model.base.BaseQueryVO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author: opensnail
 * @date : 2022-02-28 09:08
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class RetryTaskLogMessageQueryVO extends BaseQueryVO {

    private String groupName;

    private String uniqueId;

    private Long startId;

    private Integer fromIndex;
}
