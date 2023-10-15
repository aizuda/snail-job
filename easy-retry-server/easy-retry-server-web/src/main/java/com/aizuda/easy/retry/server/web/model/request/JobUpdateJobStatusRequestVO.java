package com.aizuda.easy.retry.server.web.model.request;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @author www.byteblogs.com
 * @date 2023-10-15 16:06:20
 * @since 2.4.0
 */
@Data
public class JobUpdateJobStatusRequestVO {

    @NotNull(message = "id 不能为空")
    private Long id;

    @NotNull(message = "jobStatus 不能为空")
    private Integer jobStatus;

}
