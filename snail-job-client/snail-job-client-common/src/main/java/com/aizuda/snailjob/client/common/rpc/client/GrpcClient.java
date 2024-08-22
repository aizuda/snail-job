package com.aizuda.snailjob.client.common.rpc.client;

import com.aizuda.snailjob.client.common.Lifecycle;
import com.aizuda.snailjob.client.common.config.SnailJobProperties;
import com.aizuda.snailjob.client.common.config.SnailJobProperties.ServerConfig;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * @author: opensnail
 * @date : 2024-08-22
 */
@Component
@RequiredArgsConstructor
@Order(Ordered.HIGHEST_PRECEDENCE)
public class GrpcClient implements Lifecycle {
    private final SnailJobProperties snailJobProperties;
    @Override
    public void start() {
        // 创建 gRPC 频道
        ServerConfig server = snailJobProperties.getServer();
        ManagedChannel channel = ManagedChannelBuilder.forAddress(server.getHost(), server.getPort())
            .usePlaintext()
            .build();
        GrpcChannel.setChannel(channel);
    }

    @Override
    public void close() {

    }
}
