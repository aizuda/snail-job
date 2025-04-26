package com.aizuda.snailjob.server.web.model.request;

import com.aizuda.snailjob.server.common.vo.base.BaseQueryVO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author zuoJunLin
 * @date 2023-12-02 11:16:14
 * @since 2.5.0
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class JobNotifyConfigQueryVO extends BaseQueryVO {
    private String groupName;
    private Long jobId;
}
