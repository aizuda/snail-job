package com.aizuda.snailjob.model.request.base;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 此类为更新任务的状态 【job】【workflow】【retry】共用类
 *
 * @author opensnail
 * @date 2023-10-15 16:06:20
 * @since 2.4.0
 */
@Data
public class StatusUpdateRequest {

    @NotNull(message = "id cannot be null")
    private Long id;

    @NotNull(message = "status cannot be null")
    private Integer status;

}
