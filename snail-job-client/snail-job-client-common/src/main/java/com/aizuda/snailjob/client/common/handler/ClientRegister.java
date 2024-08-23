package com.aizuda.snailjob.client.common.handler;

import com.aizuda.snailjob.client.common.Lifecycle;
import com.aizuda.snailjob.client.common.NettyClient;
import com.aizuda.snailjob.client.common.rpc.client.RequestBuilder;
import com.aizuda.snailjob.common.core.constant.SystemConstants.BEAT;
import com.aizuda.snailjob.common.core.enums.StatusEnum;
import com.aizuda.snailjob.common.core.model.SnailJobRpcResult;
import com.aizuda.snailjob.common.log.SnailJobLog;
import org.springframework.stereotype.Component;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author: oepnsnail
 * @date : 2024-07-17
 * @since : 1.1.1
 */
@Component
public class ClientRegister implements Lifecycle {
    private static final ScheduledExecutorService SCHEDULE_EXECUTOR = Executors.newSingleThreadScheduledExecutor(
        r -> new Thread(r, "sj-client-register"));
    public static final NettyClient CLIENT;
    public static final int REGISTER_TIME = 10;

    static {
        CLIENT = RequestBuilder.<NettyClient, SnailJobRpcResult>newBuilder()
            .client(NettyClient.class)
            .callback(
                nettyResult -> {
                    if (StatusEnum.NO.getStatus().equals(nettyResult.getStatus())) {
                        SnailJobLog.LOCAL.error("heartbeat check requestId:[{}] message:[{}]", nettyResult.getReqId(), nettyResult.getMessage());
                    }
                })
            .build();
    }

    @Override
    public void start() {
        SCHEDULE_EXECUTOR.scheduleAtFixedRate(() -> {
            try {
                CLIENT.beat(BEAT.PING);
            } catch (Exception e) {
                SnailJobLog.LOCAL.error("发送心跳失败", e);
            }
        }, 0, REGISTER_TIME, TimeUnit.SECONDS);
    }

    @Override
    public void close() {

    }
}
