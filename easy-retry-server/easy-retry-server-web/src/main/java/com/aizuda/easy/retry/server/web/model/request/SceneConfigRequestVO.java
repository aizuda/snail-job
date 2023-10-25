package com.aizuda.easy.retry.server.web.model.request;

import com.aizuda.easy.retry.common.core.constant.SystemConstants;
import lombok.Data;

import javax.validation.constraints.*;

/**
 * @author www.byteblogs.com
 * @date 2023-10-25 08:40:57
 * @since 2.4.0
 */
@Data
public class SceneConfigRequestVO {

    @NotBlank(message = "组名称 不能为空")
    @Pattern(regexp = "^[A-Za-z0-9_]{1,64}$", message = "仅支持长度为1~64字符且类型为数字、字母和下划线")
    private String groupName;

    @NotBlank(message = "场景名称不能为空")
    @Pattern(regexp = "^[A-Za-z0-9_]{1,64}$", message = "仅支持长度为1~64字符且类型为数字、字母和下划线")
    private String sceneName;

    @NotNull(message = "场景状态不能为空")
    private Integer sceneStatus;

    @Max(message = "最大重试次数", value = 99)
    @Min(message = "最小重试次数", value = 0)
    private Integer maxRetryCount;

    @NotNull(message = "退避策略不能为空 1、默认等级 2、固定间隔时间 3、CRON 表达式")
    private Integer backOff;

    @NotNull(message = "路由策略不能为空")
    private Integer routeKey;

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
    private Long deadlineRequest;

    /**
     * 是否删除
     */
    private Integer isDeleted;

}
