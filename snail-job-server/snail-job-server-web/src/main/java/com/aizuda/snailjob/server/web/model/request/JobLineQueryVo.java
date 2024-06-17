package com.aizuda.snailjob.server.web.model.request;

import com.aizuda.snailjob.server.common.enums.SystemModeEnum;
import lombok.Data;

@Data
public class JobLineQueryVo extends LineQueryVO {
    /**
     * 系统模式
     *
     * @see SystemModeEnum
     */
    private String mode;
}
