package com.aizuda.snailjob.model.request;

import com.aizuda.snailjob.model.dto.ExecuteResult;
import lombok.Data;

/**
 * @author: opensnail
 * @date : 2023-09-26 15:10
 */
@Data
public class DispatchJobResultRequest {

    private Long jobId;

    private Long taskBatchId;

    private Long workflowTaskBatchId;

    private Long workflowNodeId;

    private Long taskId;

    /**
     * 任务类型
     */
    private Integer taskType;

    private String groupName;

    private Integer taskStatus;

    private ExecuteResult executeResult;

    /**
     * 重试场景 auto、manual
     */
    private Integer retryScene;

    /**
     * 是否是重试流量
     */
    @Deprecated
    private boolean isRetry;

    /**
     * 是否是重试流量
     */
    private Boolean retryStatus = Boolean.FALSE;

    /**
     * 工作流上下文
     */
    private String wfContext;
}
