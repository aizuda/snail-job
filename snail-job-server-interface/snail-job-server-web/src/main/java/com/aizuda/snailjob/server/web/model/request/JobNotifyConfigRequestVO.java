package com.aizuda.snailjob.server.web.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * @author zuoJunLin
 * @date 2023-11-02 11:40:10
 * @since 2.5.0
 */
@Data
public class JobNotifyConfigRequestVO {

    private Long id;

    @NotBlank(message = "Group name cannot be null")
    @Pattern(regexp = "^[A-Za-z0-9_-]{1,64}$", message = "Only supports 1~64 characters, including numbers, letters, underscores, and hyphens")
    private String groupName;

    @NotNull(message = "Task cannot be empty")
    private Long jobId;

    @NotNull(message = "Notification status cannot be empty")
    private Integer notifyStatus;

    @NotNull(message = "Notification type cannot be empty")
    private Integer notifyType;

    @NotBlank(message = "Notification attributes cannot be empty")
    private String notifyAttribute;

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
