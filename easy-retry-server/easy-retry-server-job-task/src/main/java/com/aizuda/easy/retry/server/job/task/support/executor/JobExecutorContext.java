package com.aizuda.easy.retry.server.job.task.support.executor;

import com.aizuda.easy.retry.template.datasource.persistence.po.Job;
import com.aizuda.easy.retry.template.datasource.persistence.po.JobTask;
import lombok.Data;

import java.util.List;

/**
 * @author www.byteblogs.com
 * @date 2023-10-02 22:53:49
 * @since 2.4.0
 */
@Data
public class JobExecutorContext {

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

    private List<JobTask> taskList;

    /**
     * 执行方法参数
     */
    private String argsStr;

    /**
     * 参数类型 text/json
     */
    private Integer argsType;

    private Integer routeKey;

}
