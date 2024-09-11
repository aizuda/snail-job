package com.aizuda.snailjob.client.common.rpc.client;

import com.aizuda.snailjob.client.common.Lifecycle;
import com.aizuda.snailjob.client.common.config.SnailJobProperties;
import com.aizuda.snailjob.client.common.config.SnailJobProperties.RpcClientProperties;
import com.aizuda.snailjob.client.common.config.SnailJobProperties.ThreadPoolConfig;
import com.aizuda.snailjob.common.core.enums.RpcTypeEnum;
import com.aizuda.snailjob.common.log.SnailJobLog;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.grpc.DecompressorRegistry;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

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

    @Override
    public void start() {
        if (RpcTypeEnum.GRPC != snailJobProperties.getRpcType()) {
            return;
        }

        RpcClientProperties clientRpc = snailJobProperties.getClientRpc();
        // 创建 gRPC 频道
        String serverHost = GrpcChannel.getServerHost();
        channel = ManagedChannelBuilder.forAddress(serverHost, GrpcChannel.getServerPort())
            .executor(createGrpcExecutor(serverHost))
            .decompressorRegistry(DecompressorRegistry.getDefaultInstance())
            .maxInboundMessageSize(clientRpc.getMaxInboundMessageSize())
            .keepAliveTime(clientRpc.getKeepAliveTime().toMillis(), TimeUnit.MILLISECONDS)
            .keepAliveTimeout(clientRpc.getKeepAliveTimeout().toMillis(), TimeUnit.MILLISECONDS)
            .usePlaintext()
            .build();
        GrpcChannel.setChannel(channel);
        SnailJobLog.LOCAL.info("grpc client started connect to server");

    }

    @Override
    public void close() {
        if (channel != null && !channel.isShutdown()) {
            channel.shutdownNow();
        }
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
