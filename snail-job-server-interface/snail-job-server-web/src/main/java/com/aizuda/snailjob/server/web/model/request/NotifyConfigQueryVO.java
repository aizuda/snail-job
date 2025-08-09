package com.aizuda.snailjob.server.web.model.request;

import com.aizuda.snailjob.server.service.dto.base.BaseQueryDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author opensnail
 * @date 2023-10-25 10:16:14
 * @since 2.4.0
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class NotifyConfigQueryVO extends BaseQueryDTO {
    private String groupName;
    private String sceneName;
    private Integer systemTaskType;
    private Integer notifyStatus;
    private String notifyName;
}
