package com.aizuda.easy.retry.server.web.model.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

/**
 * @author www.byteblogs.com
 * @date 2023-10-11 22:37:55
 * @since 2.4.0
 */
@Data
public class JobRequestVO {

    private Long id;

    /**
     * 组名称
     */
    @NotBlank(message = "groupName 不能为空")
    @Pattern(regexp = "^[A-Za-z0-9_]{1,64}$", message = "仅支持长度为1~64字符且类型为数字、字母和下划线")
    private String groupName;

    /**
     * 名称
     */
    @NotBlank(message = "jobName 不能为空")
    private String jobName;

    /**
     * 执行方法参数
     */
    private String argsStr;

    /**
     * 参数类型 text/json
     */
    @NotNull(message = "argsType 不能为空")
    private Integer argsType;

    /**
     * 执行器路由策略
     */
    @NotNull(message = "routeKey 不能为空")
    private String routeKey;

    /**
     * 执行器类型 1、Java
     */
    @NotNull(message = "executorType 不能为空")
    private Integer executorType;

    /**
     * 执行器名称
     */
    @NotBlank(message = "executorName 不能为空")
    private String executorName;

    /**
     * 触发类型 1.CRON 表达式 2. 固定时间
     */
    @NotNull(message = "triggerType 不能为空")
    private Integer triggerType;

    /**
     * 间隔时长
     */
    @NotNull(message = "triggerInterval 不能为空")
    private String triggerInterval;

    /**
     * 阻塞策略 1、丢弃 2、覆盖 3、并行
     */
    @NotNull(message = "blockStrategy 不能为空")
    private Integer blockStrategy;

    /**
     * 任务执行超时时间，单位秒
     */
    @NotNull(message = "executorTimeout 不能为空")
    private Integer executorTimeout;

    /**
     * 最大重试次数
     */
    @NotNull(message = "maxRetryTimes 不能为空")
    private Integer maxRetryTimes;

    /**
     * 重试间隔(s)
     */
    @NotNull(message = "retryInterval 不能为空")
    private Integer retryInterval;

    /**
     * 任务类型
     */
    @NotNull(message = "taskType 不能为空")
    private Integer taskType;

    /**
     * 并行数
     */
    @NotNull(message = "parallelNum 不能为空")
    private Integer parallelNum;

    /**
     * 描述
     */
    private String description;

}
