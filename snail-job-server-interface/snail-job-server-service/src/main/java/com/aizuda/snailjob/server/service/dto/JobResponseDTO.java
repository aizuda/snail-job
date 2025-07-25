package com.aizuda.snailjob.server.service.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * @author opensnail
 * @date 2023-10-11 22:30:00
 * @since 2.4.0
 */
@Data
public class JobResponseDTO {

    private Long id;

    /**
     * 组名称
     */
    private String groupName;

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

    /**
     * 下次触发时间
     */
    private LocalDateTime nextTriggerAt;

    /**
     * 重试状态 0、关闭、1、开启
     */
    private Integer jobStatus;

    /**
     * 执行器路由策略
     */
    private Integer routeKey;

    /**
     * 执行器类型 1、Java
     */
    private Integer executorType;

    /**
     * 执行器名称
     */
    private String executorInfo;

    /**
     * 触发类型 1.CRON 表达式 2. 固定时间
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
     */
    private Integer taskType;

    /**
     * 并行数
     */
    private Integer parallelNum;

    /**
     * bucket
     */
    private Integer bucketIndex;

    /**
     * 描述
     */
    private String description;

    /**
     * 创建时间
     */
    private LocalDateTime createDt;

    /**
     * 修改时间
     */
    private LocalDateTime updateDt;

    /**
     * 逻辑删除 1、删除
     */
    private Integer deleted;

    /**
     * 通知告警场景
     */
    private Set<Long> notifyIds;

    /**
     * 标签
     * json格式，如：{"key1":"value1","key2":"value2"}
     */
    private String labels;

}
