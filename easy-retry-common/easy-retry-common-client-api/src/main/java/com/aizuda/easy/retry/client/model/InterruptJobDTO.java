package com.aizuda.easy.retry.client.model;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @author: www.byteblogs.com
 * @date : 2023-09-26 15:10
 */
@Data
public class InterruptJobDTO {

    @NotNull(message = "jobId 不能为空")
    private Long jobId;

    @NotNull(message = "taskId 不能为空")
    private Long taskId;

    @NotBlank(message = "group 不能为空")
    private String groupName;

}
