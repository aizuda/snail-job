package com.x.retry.server.web.model.request;

import lombok.Data;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author: www.byteblogs.com
 * @date : 2021-11-22 13:45
 */
@Data
public class GroupConfigRequestVO {

    @NotBlank(message = "组名称不能为空")
    private String groupName;

    @NotNull(message = "组状态不能为空")
    private Integer groupStatus;

    /**
     * 描述
     */
    private String description;

    /**
     * 分区
     */
    private Integer groupPartition;

    /**
     * 路由策略
     */
    private Integer routeKey;

    /**
     * 通知列表
     */
    private List<NotifyConfigVO> notifyList;

    /**
     * 场景列表
     */
    private List<SceneConfigVO> sceneList;

    @Data
    public static class NotifyConfigVO {

        @NotNull(message = "通知类型不能为空")
        private Integer notifyType;

        @NotBlank(message = "通知属性不能为空")
        private String notifyAttribute;

        @NotNull(message = "通知阈值不能为空")
        private Integer notifyThreshold;

        @NotNull(message = "通知场景不能为空")
        private Integer notifyScene;

        /**
         * 描述
         */
        private String description;

        /**
         * 是否删除
         */
        private Integer isDeleted;

    }

    @Data
    public static class SceneConfigVO {

        @NotBlank(message = "场景名称不能为空")
        private String sceneName;

        @NotNull(message = "场景状态不能为空")
        private Integer sceneStatus;

        @Max(message = "最大重试次数", value = 99)
        @Min(message = "最小重试次数", value = 0)
        private Integer maxRetryCount;

        @NotNull(message = "退避策略不能为空 1、默认等级 2、固定间隔时间 3、CRON 表达式")
        private Integer backOff;

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
        @Max(message = "最大60000毫秒", value = 60000)
        @Min(message = "最小100ms", value = 100)
        private Long deadlineRequest;

        /**
         * 是否删除
         */
        private Integer isDeleted;
    }

}
