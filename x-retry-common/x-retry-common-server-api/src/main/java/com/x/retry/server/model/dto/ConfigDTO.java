package com.x.retry.server.model.dto;

import com.x.retry.common.core.enums.NotifySceneEnum;
import com.x.retry.common.core.enums.NotifyTypeEnum;
import lombok.Data;
import java.util.List;
import java.util.Set;

/**
 * 同步的配置数据结构
 *
 * @author: www.byteblogs.com
 * @date : 2021-11-22 13:45
 */
@Data
public class ConfigDTO {

    /**
     * 场景
     */
    private List<Scene> sceneList;

    /**
     * 通知
     */
    private List<Notify> notifyList;

    /**
     * 场景黑名单
     */
    private Set<String> sceneBlacklist;

    /**
     * 版本号
     */
    private Integer version;

    @Data
    public static class Scene {
    }

    @Data
    public static class Notify {

        /**
         * 通知类型 {@link NotifyTypeEnum}
         */
        private Integer notifyType;

        /**
         * 通知地址
         */
        private String notifyAttribute;

        /**
         * 触发阈值
         */
        private Integer notifyThreshold;

        /**
         * 场景场景 {@link NotifySceneEnum}
         */
        private Integer notifyScene;
    }


}
