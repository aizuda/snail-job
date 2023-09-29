package com.aizuda.easy.retry.server.job.task.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;


/**
 * @author www.byteblogs.com
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
     * 扩展字段
     */
    private String extAttrs;


    private Long taskBatchId;

    private Long taskId;

    private Integer taskType;

    private String groupName;

    private Integer parallelNum;

    private Integer executorType;

    private String executorName;

    private String clientId;

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

}
