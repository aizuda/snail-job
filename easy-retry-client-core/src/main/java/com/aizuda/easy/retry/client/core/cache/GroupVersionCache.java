package com.aizuda.easy.retry.client.core.cache;

import com.aizuda.easy.retry.client.core.client.request.ConfigHttpRequestHandler;
import com.aizuda.easy.retry.client.core.client.request.RequestParam;
import com.aizuda.easy.retry.client.core.client.response.EasyRetryResponse;
import com.aizuda.easy.retry.client.core.Lifecycle;
import com.aizuda.easy.retry.client.core.client.NettyHttpConnectClient;
import com.aizuda.easy.retry.common.core.context.SpringContext;
import com.aizuda.easy.retry.common.core.log.LogUtils;
import com.aizuda.easy.retry.common.core.model.EasyRetryRequest;
import com.aizuda.easy.retry.server.model.dto.ConfigDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * @author: www.byteblogs.com
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
            EasyRetryRequest easyRetryRequest = new EasyRetryRequest(getVersion());

            ConfigHttpRequestHandler configHttpRequestHandler = SpringContext.getBeanByType(ConfigHttpRequestHandler.class);
            EasyRetryResponse.cache(easyRetryRequest, configHttpRequestHandler.callable());
            NettyHttpConnectClient.send(configHttpRequestHandler.method(), configHttpRequestHandler.getHttpUrl(
                    new RequestParam()), configHttpRequestHandler.body(easyRetryRequest));

        } catch (Exception e) {
            LogUtils.error(log, "同步版本失败", e);
        }
    }

    @Override
    public void close() {

    }
}
