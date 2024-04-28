package com.aizuda.snailjob.client.common.cache;

import com.aizuda.snailjob.client.common.Lifecycle;
import com.aizuda.snailjob.client.common.NettyClient;
import com.aizuda.snailjob.client.common.rpc.client.RequestBuilder;
import com.aizuda.snailjob.common.core.constant.SystemConstants;
import com.aizuda.snailjob.common.log.SnailJobLog;
import com.aizuda.snailjob.common.core.model.NettyResult;
import com.aizuda.snailjob.common.core.util.JsonUtil;
import com.aizuda.snailjob.server.model.dto.ConfigDTO;
import com.aizuda.snailjob.client.common.rpc.client.RequestBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * @author: opensnail
 * @date : 2022-05-02 21:06
 */
@Component
@Order
@Slf4j
public class GroupVersionCache implements Lifecycle {

    private static ConfigDTO CONFIG;

    public static void setConfig(ConfigDTO config) {
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
        if (Objects.isNull(CONFIG)){
            return SystemConstants.DEFAULT_DDL;
        }
        List<ConfigDTO.Scene> sceneList = CONFIG.getSceneList();
        if (CollectionUtils.isEmpty(sceneList)) {
            return SystemConstants.DEFAULT_DDL;
        }

        for (ConfigDTO.Scene scene : sceneList) {
            if (scene.getSceneName().equals(sceneName)) {
                return scene.getDdl();
            }
        }

        return SystemConstants.DEFAULT_DDL;
    }

    public static ConfigDTO.Notify getNotifyAttribute(Integer notifyScene) {
        List<ConfigDTO.Notify> notifyList = CONFIG.getNotifyList();
        for (ConfigDTO.Notify notify : notifyList) {
            if (notify.getNotifyScene().equals(notifyScene)) {
                return notify;
            }
        }

        return null;
    }


    @Override
    public void start() {

        try {
            NettyClient client = RequestBuilder.<NettyClient, NettyResult>newBuilder()
                .client(NettyClient.class)
                .callback(nettyResult -> {
                    if (Objects.isNull(nettyResult.getData())) {
                        SnailJobLog.LOCAL.error("获取配置结果为null");
                        return;
                    }

                    GroupVersionCache.setConfig(JsonUtil.parseObject(nettyResult.getData().toString(), ConfigDTO.class));
                })
                .build();
            client.getConfig(0);
        } catch (Exception e) {
            SnailJobLog.LOCAL.error("同步版本失败", e);
        }
    }

    @Override
    public void close() {

    }
}
