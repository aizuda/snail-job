package com.aizuda.snailjob.server.web.model.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.util.List;
import java.util.Set;

/**
 * @author opensnail
 * @date 2023-10-25 08:40:10
 * @since 2.4.0
 */
@Data
public class NotifyConfigRequestVO {

    private Long id;

    @NotBlank(message = "组名称 不能为空")
    @Pattern(regexp = "^[A-Za-z0-9_]{1,64}$", message = "仅支持长度为1~64字符且类型为数字、字母和下划线")
    private String groupName;

    /**
     * 业务id (scene_name或job_id或workflow_id)
     */
    @NotBlank(message = "业务id不能为空")
    private String businessId;

    /**
     * 任务类型 1、重试任务 2、回调任务、 3、JOB任务 4、WORKFLOW任务
     */
    @NotNull(message = "任务类型不能为空")
    private Integer systemTaskType;

    @NotNull(message = "通知状态不能为空")
    private Integer notifyStatus;

    @NotEmpty(message = "通知人列表")
    private Set<Long> notifyRecipientIds;

    private Integer notifyThreshold;

    @NotNull(message = "通知场景不能为空")
    private Integer notifyScene;

    @NotNull(message = "限流状态不能为空")
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
