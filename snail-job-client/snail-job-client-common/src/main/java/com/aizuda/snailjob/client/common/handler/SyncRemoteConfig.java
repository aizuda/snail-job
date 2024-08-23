package com.aizuda.snailjob.client.common.handler;

import com.aizuda.snailjob.client.common.Lifecycle;
import com.aizuda.snailjob.client.common.NettyClient;
import com.aizuda.snailjob.client.common.cache.GroupVersionCache;
import com.aizuda.snailjob.client.common.rpc.client.RequestBuilder;
import com.aizuda.snailjob.common.core.model.SnailJobRpcResult;
import com.aizuda.snailjob.common.core.util.JsonUtil;
import com.aizuda.snailjob.common.log.SnailJobLog;
import com.aizuda.snailjob.server.model.dto.ConfigDTO;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author: opensnail
 * @date : 2024-05-08
 * @since : sj_1.0.0
 */
@Component
public class SyncRemoteConfig implements Lifecycle {

    private static final ScheduledExecutorService SCHEDULE_EXECUTOR = Executors.newSingleThreadScheduledExecutor(
            r -> new Thread(r, "sync-remote-config"));

    @Override
    public void start() {

        SCHEDULE_EXECUTOR.scheduleAtFixedRate(() -> {
            try {
                NettyClient client = RequestBuilder.<NettyClient, SnailJobRpcResult>newBuilder()
                        .client(NettyClient.class)
                        .timeout(1000L)
                        .callback(nettyResult -> {
                            if (Objects.isNull(nettyResult.getData())) {
                                SnailJobLog.LOCAL.debug("获取配置结果为null");
                                return;
                            }

                            GroupVersionCache.setConfig(
                                    JsonUtil.parseObject(nettyResult.getData().toString(), ConfigDTO.class));
                        }).build();
                client.syncRemoteConfig();
            } catch (Exception e) {
                SnailJobLog.LOCAL.error("通知配置失败", e);
            }
        }, 0, 1, TimeUnit.MINUTES);
    }

    @Override
    public void close() {
    }
}
