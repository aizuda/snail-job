package com.aizuda.snailjob.client.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * @author: opensnail
 * @date : 2023-09-26 15:10
 */
@Data
public class StopJobDTO {

    @NotNull(message = "jobId cannot be null")
    private Long jobId;

    @NotNull(message = "taskBatchId cannot be null")
    private Long taskBatchId;

    @NotBlank(message = "group cannot be null")
    private String groupName;

}
