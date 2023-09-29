package com.aizuda.easy.retry.client.model.request;

import com.aizuda.easy.retry.client.model.ExecuteResult;
import lombok.Data;

/**
 * @author: www.byteblogs.com
 * @date : 2023-09-26 15:10
 */
@Data
public class DispatchJobResultRequest {

    private Long jobId;

    private Long taskBatchId;

    private Long taskId;

    /**
     * 任务类型
     */
    private Integer taskType;

    private String groupName;

    private Integer taskStatus;

    private ExecuteResult executeResult;

}
