package com.aizuda.snailjob.server.job.task.dto;

import com.aizuda.snailjob.common.core.enums.MapReduceStageEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;


/**
 * @author opensnail
 * @date 2023-10-06 16:45:13
 * @since 2.4.0
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class RealJobExecutorDTO extends BaseDTO {

    private Long jobId;

    /**
     * 名称
     */
    private String jobName;

    /**
     * 执行方法参数
     */
    private String argsStr;

    /**
     * 参数类型 text/json
     */
    private String argsType;

    /**
     * 任务名称
     */
    private String taskName;

    /**
     * 动态分片的阶段
     * {@link MapReduceStageEnum}
     */
    private Integer mrStage;

    /**
     * 扩展字段
     */
    private String extAttrs;


    private Long taskBatchId;

    private Long taskId;

    private Integer taskType;

    private String groupName;

    private Integer parallelNum;

    private Integer executorType;

    private String executorInfo;

    private String clientId;

    /**
     * 最大重试次数
     */
    private Integer maxRetryTimes;

    /**
     * 重试间隔(s)
     */
    private Integer retryInterval;

    private Integer retryCount;

    private Integer shardingTotal;

    private Integer shardingIndex;

    private Integer executorTimeout;

    /**
     * 工作流任务批次id
     */
    private Long workflowTaskBatchId;

    private Long workflowNodeId;

    /**
     * 重试场景 auto、manual
     */
    private Integer retryScene;

    /**
     * 是否是重试流量
     */
    private Boolean retryStatus = Boolean.FALSE;

    /**
     * 工作流上下文
     */
    private String wfContext;

    private Integer routeKey;

    private String labels;
}
