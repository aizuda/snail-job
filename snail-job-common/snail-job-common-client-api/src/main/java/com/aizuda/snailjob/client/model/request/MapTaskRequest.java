package com.aizuda.snailjob.client.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * @author: opensnail
 * @date : 2024-06-12 15:10
 */
@Data
public class MapTaskRequest {

    @NotNull(message = "jobId 不能为空")
    private Long jobId;

    @NotNull(message = "taskBatchId 不能为空")
    private Long taskBatchId;

    @NotNull(message = "parentId 不能为空")
    private Long parentId;

    private Long workflowTaskBatchId;

    private Long workflowNodeId;

    @NotBlank(message = "taskName 不能为空")
    private String taskName;

    @NotEmpty(message = "subTask 不能为空")
    private List<Object> subTask;

}
