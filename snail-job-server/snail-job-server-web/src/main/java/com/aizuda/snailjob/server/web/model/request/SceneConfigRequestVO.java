package com.aizuda.snailjob.server.web.model.request;

import com.aizuda.snailjob.common.core.constant.SystemConstants;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.Set;

/**
 * @author opensnail
 * @date 2023-10-25 08:40:57
 * @since 2.4.0
 */
@Data
public class SceneConfigRequestVO {

    @NotBlank(message = "组名称 不能为空")
    @Pattern(regexp = "^[A-Za-z0-9_-]{1,64}$", message = "仅支持长度为1~64字符且类型为数字、字母、下划线和短横线")
    private String groupName;

    @NotBlank(message = "场景名称不能为空")
    @Pattern(regexp = "^[A-Za-z0-9_-]{1,64}$", message = "仅支持长度为1~64字符且类型为数字、字母、下划线和短横线")
    private String sceneName;

    @NotNull(message = "场景状态不能为空")
    private Integer sceneStatus;

    @Max(message = "最大重试次数", value = 9999999)
    @Min(message = "最小重试次数", value = 0)
    private Integer maxRetryCount;

    @NotNull(message = "退避策略不能为空 1、默认等级 2、固定间隔时间 3、CRON 表达式")
    private Integer backOff;

    @NotNull(message = "路由策略不能为空")
    private Integer routeKey;

    /**
     * @see: RetryBlockStrategyEnum
     */
    @NotNull(message = "阻塞策略不能为空")
    private Integer blockStrategy;

    /**
     * 描述
     */
    private String description;

    /**
     * 退避策略为固定间隔时间必填
     */
    private String triggerInterval;

    /**
     * Deadline Request 调用链超时 单位毫秒
     * 默认值为 60*10*1000
     */
    @Max(message = "最大60000毫秒", value = SystemConstants.DEFAULT_DDL)
    @Min(message = "最小100ms", value = 100)
    @NotNull(message = "调用链超时不能为空")
    private Long deadlineRequest;

    @Max(message = "最大60(秒)", value = 60)
    @Min(message = "最小1(秒)", value = 1)
    @NotNull(message = "执行超时不能为空")
    private Integer executorTimeout;

    /**
     * 是否删除
     */
    private Integer isDeleted;

    /**
     * 通知告警场景配置id列表
     */
    private Set<Long> notifyIds;

    /**
     * 回调状态 0、不开启 1、开启
     */
    @NotNull(message = "回调状态不能为空")
    private Integer cbStatus;

    /**
     * 回调触发类型
     */
    @NotNull(message = "回调触发类型不能为空")
    private Integer cbTriggerType;

    /**
     * 回调的最大执行次数
     */
    @NotNull(message = "回调的最大执行次数不能为空")
    private int cbMaxCount;

    /**
     * 回调间隔时间
     */
    private String cbTriggerInterval;
}
