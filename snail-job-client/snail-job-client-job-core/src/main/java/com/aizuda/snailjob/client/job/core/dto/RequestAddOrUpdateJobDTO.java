package com.aizuda.snailjob.client.job.core.dto;

import com.aizuda.snailjob.client.job.core.handler.add.Add;
import com.aizuda.snailjob.client.job.core.handler.update.Update;
import com.aizuda.snailjob.client.job.core.handler.update.UpdateHandler;
import com.aizuda.snailjob.common.core.enums.ExecutorTypeEnum;
import com.aizuda.snailjob.common.core.enums.JobTaskTypeEnum;
import com.aizuda.snailjob.common.core.enums.StatusEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class RequestAddOrUpdateJobDTO {

    @NotNull(message = "id cannot be null", groups = Update.class)
    private Long id;

    /**
     * 组名称
     */
    @NotBlank(message = "groupName cannot be null")
    @Pattern(regexp = "^[A-Za-z0-9_-]{1,64}$", message = "Only supports 1~64 characters, including numbers, letters, underscores, and hyphens")
    private String groupName;

    /**
     * 名称
     */
    @NotBlank(message = "jobName cannot be null", groups = Add.class)
    private String jobName;

    /**
     * 重试状态 0、关闭、1、开启
     * {@link StatusEnum}
     */
    @NotNull(message = "jobStatus cannot be null", groups = Add.class)
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
    @NotNull(message = "routeKey cannot be null", groups = Add.class)
    private Integer routeKey;

    /**
     * 执行器类型
     * {@link ExecutorTypeEnum}
     */
    @NotNull(message = "executorType cannot be null", groups = Add.class)
    private Integer executorType;

    /**
     * 执行器名称
     */
    @NotBlank(message = "executorInfo cannot be null", groups = Add.class)
    private String executorInfo;

    /**
     * 触发类型 2. 固定时间 3.CRON 表达式 99.工作流
     */
    @NotNull(message = "triggerType cannot be null", groups = Add.class)
    private Integer triggerType;

    /**
     * 间隔时长
     */
    @NotNull(message = "triggerInterval cannot be null", groups = Add.class)
    private String triggerInterval;

    /**
     * 阻塞策略 1、丢弃 2、覆盖 3、并行
     */
    @NotNull(message = "blockStrategy cannot be null", groups = Add.class)
    private Integer blockStrategy;

    /**
     * 任务执行超时时间，单位秒
     */
    @NotNull(message = "executorTimeout cannot be null", groups = Add.class)
    private Integer executorTimeout;

    /**
     * 最大重试次数
     */
    @NotNull(message = "maxRetryTimes cannot be null", groups = Add.class)
    private Integer maxRetryTimes;

    /**
     * 重试间隔(s)
     */
    @NotNull(message = "retryInterval cannot be null", groups = Add.class)
    private Integer retryInterval;

    /**
     * 任务类型
     * {@link JobTaskTypeEnum}
     */
    @NotNull(message = "taskType cannot be null", groups = Add.class)
    private Integer taskType;

    /**
     * 并行数
     */
    @NotNull(message = "parallelNum cannot be null", groups = Add.class)
    private Integer parallelNum;

    /**
     * 描述
     */
    private String description;

    /**
     * 标签
     * json格式，如：{"key1":"value1","key2":"value2"}
     */
    private String labels;
}
