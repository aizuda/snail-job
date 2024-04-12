package com.aizuda.easy.retry.client.common.cache;

import com.aizuda.easy.retry.client.common.Lifecycle;
import com.aizuda.easy.retry.client.common.NettyClient;
import com.aizuda.easy.retry.client.common.rpc.client.RequestBuilder;
import com.aizuda.easy.retry.common.core.constant.SystemConstants;
import com.aizuda.easy.retry.common.log.EasyRetryLog;
import com.aizuda.easy.retry.common.core.model.NettyResult;
import com.aizuda.easy.retry.common.core.util.JsonUtil;
import com.aizuda.easy.retry.server.model.dto.ConfigDTO;
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

    public static ConfigDTO configDTO;

    public static Integer getVersion() {
        if (Objects.isNull(configDTO)) {
            return 0;
        }
        return configDTO.getVersion();
    }

    public static long getDdl(String sceneName) {
        // 缓存初始化时configDTO值为null,可能造成空指针异常
        if (Objects.isNull(configDTO)){
            return SystemConstants.DEFAULT_DDL;
        }
        List<ConfigDTO.Scene> sceneList = configDTO.getSceneList();
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

    public static Set<String> getSceneBlacklist() {
        if (Objects.isNull(configDTO)) {
            return new HashSet<>();
        }

        return configDTO.getSceneBlacklist();
    }

    public static ConfigDTO.Notify getNotifyAttribute(Integer notifyScene) {
        List<ConfigDTO.Notify> notifyList = configDTO.getNotifyList();
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
                        EasyRetryLog.LOCAL.error("获取配置结果为null");
                        return;
                    }

                    GroupVersionCache.configDTO = JsonUtil.parseObject(nettyResult.getData().toString(), ConfigDTO.class);
                })
                .build();
            client.getConfig(0);
        } catch (Exception e) {
            EasyRetryLog.LOCAL.error("同步版本失败", e);
        }
    }

    @Override
    public void close() {

    }
}
