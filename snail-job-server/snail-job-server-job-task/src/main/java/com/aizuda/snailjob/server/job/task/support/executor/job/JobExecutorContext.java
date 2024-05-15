package com.aizuda.snailjob.server.job.task.support.executor.job;

import com.aizuda.snailjob.template.datasource.persistence.po.JobTask;
import lombok.Data;

import java.util.List;

/**
 * @author opensnail
 * @date 2023-10-02 22:53:49
 * @since 2.4.0
 */
@Data
public class JobExecutorContext {

    /**
     * 命名空间id
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

    /**
     * 扩展字段
     */
    private String extAttrs;


    private Long taskId;


    private Integer parallelNum;

    private Integer executorType;

    private String executorInfo;


    /**
     * 最大重试次数
     */
    private Integer maxRetryTimes;

    /**
     * 重试间隔(s)
     */
    private Integer retryInterval;

    private Integer shardingTotal;

    private Integer shardingIndex;

    private Integer executorTimeout;
    /**
     * 工作流任务批次id
     */
    private Long workflowTaskBatchId;

    private Long workflowNodeId;

}
