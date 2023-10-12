package com.aizuda.easy.retry.server.job.task.support.stop;

import com.aizuda.easy.retry.template.datasource.persistence.po.JobTask;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author www.byteblogs.com
 * @date 2023-10-02 22:53:49
 * @since 2.4.0
 */
@Data
public class TaskStopJobContext {

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
     * 下次触发时间
     */
    private LocalDateTime nextTriggerAt;

    /**
     * 是否需要变更任务状态
     */
    private boolean needUpdateTaskStatus;

    private List<JobTask> jobTasks;

}
