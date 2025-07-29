package com.aizuda.snailjob.server.job.task.support.callback;

import com.aizuda.snailjob.model.dto.ExecuteResult;
import com.aizuda.snailjob.template.datasource.persistence.po.Job;
import com.aizuda.snailjob.template.datasource.persistence.po.JobTask;
import lombok.Data;

/**
 * @author opensnail
 * @date 2023-10-03 23:13:05
 * @since 2.4.0
 */
@Data
public class ClientCallbackContext {

    private Long jobId;

    /**
     * 命名空间
     */
    private String namespaceId;

    private Long taskBatchId;

    /**
     * 工作流任务批次id
     */
    private Long workflowTaskBatchId;

    private Long workflowNodeId;

    private Long taskId;

    private String groupName;

    private Integer taskStatus;

    private ExecuteResult executeResult;

    private String clientInfo;

    private Job job;

    private JobTask jobTask;

    private Integer retryScene;

    @Deprecated
    private boolean isRetry;

    /**
     * 是否是重试流量
     */
    private Boolean retryStatus;
    /**
     * 工作流上下文
     */
    private String wfContext;

    /**
     * 标签
     * json格式，如：{"key1":"value1","key2":"value2"}
     */
    private String labels;

    // 兼容isRetry/retryStatus并存
    @Deprecated
    public Boolean getRetryStatus() {
        return Boolean.TRUE.equals(retryStatus) || isRetry;
    }

    // 兼容isRetry/retryStatus并存
    @Deprecated
    public void setRetryStatus(boolean value) {
        this.retryStatus = Boolean.valueOf(value);
        isRetry = value;
    }

}
