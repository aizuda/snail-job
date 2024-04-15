package com.aizuda.snail.job.template.datasource.persistence.dataobject;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author opensnail
 * @date 2023-10-15 23:03:01
 * @since 2.4.0
 */
@Data
public class JobBatchResponseDO {

    private Long id;

    /**
     * 命名空间
     */
    private String namespaceId;

    /**
     * 组名称
     */
    private String groupName;

    /**
     * 名称
     */
    private String jobName;

    /**
     * 任务信息id
     */
    private Long jobId;

    /**
     * 任务状态
     */
    private Integer taskBatchStatus;

    /**
     * 创建时间
     */
    private LocalDateTime createDt;

    /**
     * 任务执行时间
     */
    private Long executionAt;

    /**
     * 操作原因
     */
    private Integer operationReason;

    /**
     * 执行器类型 1、Java
     */
    private Integer executorType;

    /**
     * 执行器名称
     */
    private String executorInfo;


    private Integer taskType;
    private Integer blockStrategy;
    private Integer triggerType;

    private String argsStr;



}
