package com.aizuda.snailjob.client.job.core.dto;

import com.aizuda.snailjob.common.core.enums.ExecutorTypeEnum;
import com.aizuda.snailjob.common.core.enums.JobTaskTypeEnum;
import com.aizuda.snailjob.common.core.enums.StatusEnum;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * @author opensnail
 * @date 2023-10-11 22:37:55
 * @since 2.4.0
 */
@Data
public class RequestUpdateJobDTO {
    @NotNull(message = "id 不能为空")
    private Long id;

    /**
     * 名称
     */
    private String jobName;

    /**
     * 重试状态 0、关闭、1、开启
     * {@link StatusEnum}
     */
    private Integer jobStatus;

    /**
     * 执行方法参数
     */
    private String argsStr;

    /**
     * 参数类型 text/json
     */
    private Integer argsType;

    /**
     * 执行器路由策略
     */
    private Integer routeKey;

    /**
     * 执行器类型
     * {@link ExecutorTypeEnum}
     */
    private Integer executorType;

    /**
     * 执行器名称
     */
    private String executorInfo;

    /**
     * 触发类型 2. 固定时间 3.CRON 表达式 99.工作流
     */
    private Integer triggerType;

    /**
     * 间隔时长
     */
    private String triggerInterval;

    /**
     * 阻塞策略 1、丢弃 2、覆盖 3、并行
     */
    private Integer blockStrategy;

    /**
     * 任务执行超时时间，单位秒
     */
    private Integer executorTimeout;

    /**
     * 最大重试次数
     */
    private Integer maxRetryTimes;

    /**
     * 重试间隔(s)
     */
    private Integer retryInterval;

    /**
     * 任务类型
     * {@link JobTaskTypeEnum}
     */
    private Integer taskType;

    /**
     * 并行数
     */
    private Integer parallelNum;

    /**
     * 描述
     */
    private String description;

}
