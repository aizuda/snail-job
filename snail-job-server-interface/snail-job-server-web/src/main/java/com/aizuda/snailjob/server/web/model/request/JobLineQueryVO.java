package com.aizuda.snailjob.server.web.model.request;

import com.aizuda.snailjob.server.common.enums.SystemModeEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class JobLineQueryVO extends LineQueryVO {
    /**
     * 系统模式
     *
     * @see SystemModeEnum
     */
    private String mode;
}
