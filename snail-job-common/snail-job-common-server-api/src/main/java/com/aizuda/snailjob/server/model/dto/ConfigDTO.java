package com.aizuda.snailjob.server.model.dto;

import com.aizuda.snailjob.common.core.constant.SystemConstants;
import com.aizuda.snailjob.common.core.enums.AlarmTypeEnum;
import com.aizuda.snailjob.common.core.enums.RetryNotifySceneEnum;
import lombok.Data;
import java.util.List;
import java.util.Set;

/**
 * 同步的配置数据结构
 *
 * @author: opensnail
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
     * 版本号
     */
    private Integer version;

    @Data
    public static class Scene {

        /**
         * 场景名称
         */
        private String sceneName;

        /**
         * 调用链超时时间 单位毫秒(ms)
         */
        private long ddl = SystemConstants.DEFAULT_DDL;
    }

    @Data
    public static class Notify {

        /**
         * 通知人
         */
        private List<Recipient> recipients;

        /**
         * 触发阈值
         */
        private Integer notifyThreshold;

        /**
         * 场景场景 {@link RetryNotifySceneEnum}
         */
        private Integer notifyScene;

        @Data
        public static class Recipient {

            /**
             * 通知类型 {@link AlarmTypeEnum}
             */
            private Integer notifyType;

            /**
             * 通知地址
             */
            private String notifyAttribute;

        }
    }


}
