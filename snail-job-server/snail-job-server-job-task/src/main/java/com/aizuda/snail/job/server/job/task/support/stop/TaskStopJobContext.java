package com.aizuda.snail.job.server.job.task.support.stop;

import com.aizuda.snail.job.template.datasource.persistence.po.JobTask;
import lombok.Data;

import java.util.List;

/**
 * @author opensnail
 * @date 2023-10-02 22:53:49
 * @since 2.4.0
 */
@Data
public class TaskStopJobContext {

    /**
     * 命名空间
     */
    private String namespaceId;

    /**
     * 组名称
     */
    private String groupName;

    /**
     * 任务id
     */
    private Long jobId;

    /**
     * 任务id
     */
    private Long taskBatchId;

    /**
     * 任务类型
     */
    private Integer taskType;

    /**
     * 是否需要变更任务状态
     */
    private boolean needUpdateTaskStatus;

    private List<JobTask> jobTasks;

    private Integer jobOperationReason;

    private boolean forceStop;

    protected List<JobTask> getJobTasks() {
        return jobTasks;
    }

    protected void setJobTasks(List<JobTask> jobTasks) {
        this.jobTasks = jobTasks;
    }
}
