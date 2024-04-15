package com.aizuda.snail.job.client.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * @author: opensnail
 * @date : 2023-09-26 15:10
 */
@Data
public class StopJobDTO {

    @NotNull(message = "jobId 不能为空")
    private Long jobId;

    @NotNull(message = "taskBatchId 不能为空")
    private Long taskBatchId;

    @NotBlank(message = "group 不能为空")
    private String groupName;

}
