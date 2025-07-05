package com.aizuda.snailjob.server.web.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.util.Set;

/**
 * @author opensnail
 * @date 2023-10-25 08:40:10
 * @since 2.4.0
 */
@Data
public class NotifyConfigRequestVO {

    private Long id;

    @NotBlank(message = "Group name cannot be null")
    @Pattern(regexp = "^[A-Za-z0-9_-]{1,64}$", message = "Only supports 1~64 characters, including numbers, letters, underscores, and hyphens")
    private String groupName;

    /**
     * 任务类型 1、重试任务 2、回调任务、 3、JOB任务 4、WORKFLOW任务
     */
    @NotNull(message = "Task type cannot be empty")
    private Integer systemTaskType;

    @NotNull(message = "Notification status cannot be empty")
    private Integer notifyStatus;

    @NotNull(message = "Notification alarm scene name cannot be empty")
    private String notifyName;

    @NotEmpty(message = "Notification recipient list")
    private Set<Long> recipientIds;

    private Integer notifyThreshold;

    @NotNull(message = "Notification scene cannot be empty")
    private Integer notifyScene;

    @NotNull(message = "Rate limiting status cannot be empty")
    private Integer rateLimiterStatus;

    private Integer rateLimiterThreshold;
    /**
     * 描述
     */
    private String description;

    /**
     * 是否删除
     */
    private Integer isDeleted;

}
