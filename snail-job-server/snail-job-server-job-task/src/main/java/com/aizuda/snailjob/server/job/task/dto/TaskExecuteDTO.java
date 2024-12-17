package com.aizuda.snailjob.server.job.task.dto;

import lombok.Data;

/**
 * @author: opensnail
 * @date : 2023-09-26 15:39
 */
@Data
public class TaskExecuteDTO {

    private Long jobId;
    private Long taskBatchId;
    /**
     * 工作流任务批次id
     */
    private Long workflowTaskBatchId;

    private Long workflowNodeId;
    /**
     * 执行策略 1、auto 2、manual 3、workflow
     */
    private Integer taskExecutorScene;

    /**
     * 临时任务参数
     */
    private String tmpArgsStr;

    public TaskExecuteDTO() {
    }

    public TaskExecuteDTO(Long jobId, Long taskBatchId, Long workflowTaskBatchId, Long workflowNodeId, Integer taskExecutorScene) {
        this.jobId = jobId;
        this.taskBatchId = taskBatchId;
        this.workflowTaskBatchId = workflowTaskBatchId;
        this.workflowNodeId = workflowNodeId;
        this.taskExecutorScene = taskExecutorScene;
    }

}
