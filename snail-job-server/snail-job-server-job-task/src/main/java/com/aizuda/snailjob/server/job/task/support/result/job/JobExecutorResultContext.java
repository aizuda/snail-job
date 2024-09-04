package com.aizuda.snailjob.server.job.task.support.result.job;

import com.aizuda.snailjob.template.datasource.persistence.po.JobTask;
import lombok.Data;

import java.util.List;

/**
 * @author: opensnail
 * @date : 2024-09-04
 * @since :1.2.0
 */
@Data
public class JobExecutorResultContext {

    private Long jobId;
    private Long workflowNodeId;
    private Long workflowTaskBatchId;
    private Long taskBatchId;
    private Integer jobOperationReason;
    private Integer taskType;
    private boolean isRetry;
    private List<JobTask> jobTaskList;

    /**
     * 是否开启创建Reduce任务
     */
    private boolean createReduceTask;

    /**
     * 是否更新批次完成
     */
    private boolean taskBatchComplete;

}
