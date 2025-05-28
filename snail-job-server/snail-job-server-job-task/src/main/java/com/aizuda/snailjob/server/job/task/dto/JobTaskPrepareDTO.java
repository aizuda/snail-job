package com.aizuda.snailjob.server.job.task.dto;

import lombok.Data;

/**
 * @author opensnail
 * @date 2023-09-25 22:42:21
 * @since 2.4.0
 */
@Data
public class JobTaskPrepareDTO {

    private Long jobId;

    private String namespaceId;

    /**
     * 组名称
     */
    private String groupName;

    /**
     * 下次触发时间
     */
    private long nextTriggerAt;

    /**
     * 阻塞策略 1、丢弃 2、覆盖 3、并行
     */
    private Integer blockStrategy;

    /**
     * 任务类型
     */
    private Integer taskType;

    /**
     * 任务执行超时时间，单位秒
     */
    private Integer executorTimeout;

    private Long taskBatchId;

    private String clientId;

    /**
     * 任务执行时间
     */
    private Long executionAt;

    private boolean onlyTimeoutCheck;

    /**
     * 执行策略 1、auto_job 2、manual_job 3、auto_workflow 4、manual_workflow
     */
    private Integer taskExecutorScene;

    /**
     * 工作流任务批次id
     */
    private Long workflowTaskBatchId;

    /**
     * 工作流节点id
     */
    private Long workflowNodeId;

    /**
     * 工作流父节点id
     */
    private Long parentWorkflowNodeId;

    /**
     * 临时任务参数
     */
    private String tmpArgsStr;

    /**
     * 标签
     * json格式，如：{"key1":"value1","key2":"value2"}
     */
    private String labels;
}
