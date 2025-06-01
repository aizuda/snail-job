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

    @NotBlank(message = "Group name cannot be null")
    @Pattern(regexp = "^[A-Za-z0-9_-]{1,64}$", message = "Only supports 1~64 characters, including numbers, letters, underscores, and hyphens")
    private String groupName;

    @NotBlank(message = "Scene name cannot be null")
    @Pattern(regexp = "^[A-Za-z0-9_-]{1,64}$", message = "Only supports 1~64 characters, including numbers, letters, underscores, and hyphens")
    private String sceneName;

    @NotNull(message = "Scene status cannot be null")
    private Integer sceneStatus;

    @Max(message = "Maximum retry times", value = 9999999)
    @Min(message = "Minimum retry times", value = 0)
    private Integer maxRetryCount;

    @NotNull(message = "Backoff strategy cannot be null 1. Default level 2. Fixed interval 3. CRON expression")
    private Integer backOff;

    @NotNull(message = "Routing strategy cannot be null")
    private Integer routeKey;

    /**
     * @see: RetryBlockStrategyEnum
     */
    @NotNull(message = "Blocking strategy cannot be null")
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
    @Max(message = "Maximum 60000 milliseconds", value = SystemConstants.DEFAULT_DDL)
    @Min(message = "Minimum 100ms", value = 100)
    @NotNull(message = "Call chain timeout cannot be null")
    private Long deadlineRequest;

    @Max(message = "Maximum 60 seconds", value = 60)
    @Min(message = "Minimum 1 second", value = 1)
    @NotNull(message = "Execution timeout cannot be null")
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
    @NotNull(message = "Callback status cannot be null")
    private Integer cbStatus;

    /**
     * 回调触发类型
     */
    @NotNull(message = "Callback trigger type cannot be null")
    private Integer cbTriggerType;

    /**
     * 回调的最大执行次数
     */
    @NotNull(message = "Maximum callback execution times cannot be null")
    private int cbMaxCount;

    /**
     * 回调间隔时间
     */
    private String cbTriggerInterval;

    /**
     * 负责人id
     */
    private Long ownerId;
}
