package com.aizuda.snailjob.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * @author: opensnail
 * @date : 2024-06-12 15:10
 */
@Data
public class MapTaskRequest {

    @NotNull(message = "jobId cannot be null")
    private Long jobId;

    @NotNull(message = "taskBatchId cannot be null")
    private Long taskBatchId;

    @NotNull(message = "parentId cannot be null")
    private Long parentId;

    private Long workflowTaskBatchId;

    private Long workflowNodeId;

    /**
     * 当前节点变更的工作流上下文
     */
    private String wfContext;

    @NotBlank(message = "taskName cannot be null")
    private String taskName;

    @NotEmpty(message = "subTask cannot be null")
    private List<Object> subTask;

}
