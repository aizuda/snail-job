package com.aizuda.easy.retry.server.job.task.support.callback;

import com.aizuda.easy.retry.client.model.ExecuteResult;
import lombok.Data;

/**
 * @author www.byteblogs.com
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


}
