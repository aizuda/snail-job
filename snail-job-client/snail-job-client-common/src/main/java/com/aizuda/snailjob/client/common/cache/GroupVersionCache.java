package com.aizuda.snailjob.client.common.cache;

import cn.hutool.core.collection.CollUtil;
import com.aizuda.snailjob.model.request.ConfigRequest;
import com.aizuda.snailjob.common.core.constant.SystemConstants;

import java.util.List;
import java.util.Objects;

/**
 * @author: opensnail
 * @date : 2022-05-02 21:06
 */
public final class GroupVersionCache {
    private GroupVersionCache() {
    }

    private static ConfigRequest CONFIG;

    public static void setConfig(ConfigRequest config) {
        GroupVersionCache.CONFIG = config;
    }

    public static Integer getVersion() {
        if (Objects.isNull(CONFIG)) {
            return 0;
        }
        return CONFIG.getVersion();
    }

    public static long getDdl(String sceneName) {
        // 缓存初始化时configDTO值为null,可能造成空指针异常
        if (Objects.isNull(CONFIG)) {
            return SystemConstants.DEFAULT_DDL;
        }
        List<ConfigRequest.Scene> sceneList = CONFIG.getSceneList();
        if (CollUtil.isEmpty(sceneList)) {
            return SystemConstants.DEFAULT_DDL;
        }

        for (ConfigRequest.Scene scene : sceneList) {
            if (scene.getSceneName().equals(sceneName)) {
                return scene.getDdl();
            }
        }

        return SystemConstants.DEFAULT_DDL;
    }

    public static ConfigRequest.Notify getRetryNotifyAttribute(Integer notifyScene) {
        List<ConfigRequest.Notify> notifyList = CONFIG.getNotifyList();
        if (CollUtil.isNotEmpty(notifyList)) {
            for (ConfigRequest.Notify notify : notifyList) {
                if (Objects.nonNull(notify.getRetryNotifyScene()) && notify.getRetryNotifyScene().equals(notifyScene)) {
                    return notify;
                }
            }
        }

        return null;
    }


    public static ConfigRequest.Notify getJobNotifyAttribute(Integer notifyScene) {
        List<ConfigRequest.Notify> notifyList = CONFIG.getNotifyList();
        if (CollUtil.isNotEmpty(notifyList)) {
            for (ConfigRequest.Notify notify : notifyList) {
                if (Objects.nonNull(notify.getJobNotifyScene()) && notify.getJobNotifyScene().equals(notifyScene)) {
                    return notify;
                }
            }
        }

        return null;
    }

}
