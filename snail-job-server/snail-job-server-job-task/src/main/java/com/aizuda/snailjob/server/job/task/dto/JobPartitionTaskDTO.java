package com.aizuda.snailjob.server.job.task.dto;

import com.aizuda.snailjob.server.common.dto.PartitionTask;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author:  opensnail
 * @date : 2023-10-10 17:52
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class JobPartitionTaskDTO extends PartitionTask {

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
     * 触发类型 1.CRON 表达式 2. 固定时间
     */
    private Integer triggerType;

    /**
     * 间隔时长
     */
    private String triggerInterval;

    /**
     * 任务执行超时时间，单位秒
     */
    private Integer executorTimeout;

    /**
     * 任务类型
     */
    private Integer taskType;

    /**
     * 是否是常驻任务
     */
    private Integer resident;
}
