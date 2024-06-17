package com.aizuda.snailjob.server.job.task.support.callback;

import com.aizuda.snailjob.client.model.ExecuteResult;
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

    private boolean isRetry;

    /**
     * 工作流上下文
     */
    private String wkContext;
}
