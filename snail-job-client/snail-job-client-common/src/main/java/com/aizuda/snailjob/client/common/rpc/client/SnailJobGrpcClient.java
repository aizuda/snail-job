package com.aizuda.snailjob.client.common.rpc.client;

import com.aizuda.snailjob.client.common.Lifecycle;
import com.aizuda.snailjob.client.common.config.SnailJobProperties;
import com.aizuda.snailjob.client.common.config.SnailJobProperties.RpcClientProperties;
import com.aizuda.snailjob.client.common.config.SnailJobProperties.ThreadPoolConfig;
import com.aizuda.snailjob.client.common.event.SnailChannelReconnectEvent;
import com.aizuda.snailjob.common.core.context.SnailSpringContext;
import com.aizuda.snailjob.common.core.enums.RpcTypeEnum;
import com.aizuda.snailjob.common.log.SnailJobLog;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.grpc.*;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.concurrent.*;


/**
 * @author: opensnail
 * @date : 2024-08-22
 */
@Component
@RequiredArgsConstructor
@Order(Ordered.HIGHEST_PRECEDENCE)
public class SnailJobGrpcClient implements Lifecycle {
    private ManagedChannel channel;
    private final SnailJobProperties snailJobProperties;
    private static final ScheduledExecutorService SCHEDULE_EXECUTOR = Executors.newSingleThreadScheduledExecutor(
            r -> new Thread(r, "sj-client-check"));
    @Override
    public void start() {
        if (RpcTypeEnum.GRPC != snailJobProperties.getRpcType()) {
            return;
        }

        channel = connection();
        GrpcChannel.setChannel(channel);
        SnailJobLog.LOCAL.info("grpc client started connect to server");

        // 连接检测
        SCHEDULE_EXECUTOR.scheduleAtFixedRate(() -> {
            ConnectivityState state = channel.getState(true);
            if (state == ConnectivityState.TRANSIENT_FAILURE) {
                try {
                    // 抛出重连事件
                    SnailSpringContext.getContext().publishEvent(new SnailChannelReconnectEvent());
                } catch (Exception e) {
                    SnailJobLog.LOCAL.error("reconnect error ", e);
                }
            }
        }, 0, 10, TimeUnit.SECONDS);

    }

    @Override
    public void close() {
        if (channel != null && !channel.isShutdown()) {
            channel.shutdownNow();
        }
    }

    public ManagedChannel connection() {
        RpcClientProperties clientRpc = snailJobProperties.getClientRpc();
        // 创建 gRPC 频道
        String serverHost = GrpcChannel.getServerHost();
        return NettyChannelBuilder.forAddress(serverHost, GrpcChannel.getServerPort())
                .executor(createGrpcExecutor(serverHost))
                .decompressorRegistry(DecompressorRegistry.getDefaultInstance())
                .maxInboundMessageSize(clientRpc.getMaxInboundMessageSize())
                .keepAliveTime(clientRpc.getKeepAliveTime().toMillis(), TimeUnit.MILLISECONDS)
                .keepAliveTimeout(clientRpc.getKeepAliveTimeout().toMillis(), TimeUnit.MILLISECONDS)
                .usePlaintext().enableRetry().maxRetryAttempts(16)
                .build();
    }

    private ThreadPoolExecutor createGrpcExecutor(String serverIp) {
        RpcClientProperties clientRpc = snailJobProperties.getClientRpc();
        ThreadPoolConfig threadPool = clientRpc.getClientTp();
        serverIp = serverIp.replaceAll("%", "-");
        ThreadPoolExecutor grpcExecutor = new ThreadPoolExecutor(threadPool.getCorePoolSize(),
            threadPool.getMaximumPoolSize(), threadPool.getKeepAliveTime(), TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<>(threadPool.getQueueCapacity()),
            new ThreadFactoryBuilder().setDaemon(true).setNameFormat("snail-job-grpc-client-executor-" + serverIp + "-%d")
                .build());
        grpcExecutor.allowCoreThreadTimeOut(true);
        return grpcExecutor;
    }
}
