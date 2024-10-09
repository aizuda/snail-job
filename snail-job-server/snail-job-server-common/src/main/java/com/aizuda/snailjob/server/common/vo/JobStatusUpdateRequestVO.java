package com.aizuda.snailjob.server.common.vo;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * @author opensnail
 * @date 2023-10-15 16:06:20
 * @since 2.4.0
 */
@Data
public class JobStatusUpdateRequestVO {

    @NotNull(message = "id 不能为空")
    private Long id;

    @NotNull(message = "jobStatus 不能为空")
    private Integer jobStatus;

}
