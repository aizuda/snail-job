package com.aizuda.snailjob.client.common.rpc.server;

import com.aizuda.snailjob.client.common.Lifecycle;
import com.aizuda.snailjob.client.common.config.SnailJobProperties;
import com.aizuda.snailjob.client.common.config.SnailJobProperties.RpcServerProperties;
import com.aizuda.snailjob.client.common.config.SnailJobProperties.ThreadPoolConfig;
import com.aizuda.snailjob.client.common.exception.SnailJobClientException;
import com.aizuda.snailjob.client.common.rpc.client.grpc.GrpcChannel;
import com.aizuda.snailjob.client.common.rpc.supports.handler.SnailDispatcherRequestHandler;
import com.aizuda.snailjob.client.common.rpc.supports.handler.grpc.UnaryRequestHandler;
import com.aizuda.snailjob.common.core.constant.GrpcServerConstants;
import com.aizuda.snailjob.common.core.enums.RpcTypeEnum;
import com.aizuda.snailjob.common.core.grpc.auto.GrpcResult;
import com.aizuda.snailjob.common.core.grpc.auto.SnailJobGrpcRequest;
import com.aizuda.snailjob.common.log.SnailJobLog;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.grpc.CompressorRegistry;
import io.grpc.DecompressorRegistry;
import io.grpc.MethodDescriptor;
import io.grpc.Server;
import io.grpc.ServerInterceptor;
import io.grpc.ServerInterceptors;
import io.grpc.ServerServiceDefinition;
import io.grpc.netty.shaded.io.grpc.netty.NettyServerBuilder;
import io.grpc.protobuf.ProtoUtils;
import io.grpc.stub.ServerCalls;
import io.grpc.util.MutableHandlerRegistry;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Grpc server
 *
 * @author: opensnail
 * @date : 2024-04-12 23:03
 * @since 3.3.0
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
@Getter
public class SnailGrpcServer implements Lifecycle {
    private final SnailJobProperties snailJobProperties;
    private final SnailDispatcherRequestHandler snailDispatcherRequestHandler;
    private volatile boolean started = false;
    private Server server;

    @Override
    public void start() {
        if (started || RpcTypeEnum.GRPC != snailJobProperties.getRpcType()) {
            return;
        }

        RpcServerProperties grpc = snailJobProperties.getServerRpc();

        final MutableHandlerRegistry handlerRegistry = new MutableHandlerRegistry();
        addServices(handlerRegistry, new GrpcInterceptor());
        NettyServerBuilder builder = NettyServerBuilder.forPort(GrpcChannel.getClientPort())
            .executor(createGrpcExecutor(grpc.getDispatcherTp()));

        Duration keepAliveTime = grpc.getKeepAliveTime();
        Duration keepAliveTimeOut = grpc.getKeepAliveTimeout();
        Duration permitKeepAliveTime = grpc.getPermitKeepAliveTime();

        server = builder.maxInboundMessageSize(grpc.getMaxInboundMessageSize()).fallbackHandlerRegistry(handlerRegistry)
            .compressorRegistry(CompressorRegistry.getDefaultInstance())
            .decompressorRegistry(DecompressorRegistry.getDefaultInstance())
            .keepAliveTime(keepAliveTime.toMillis(), TimeUnit.MILLISECONDS)
            .keepAliveTimeout(keepAliveTimeOut.toMillis(), TimeUnit.MILLISECONDS)
            .permitKeepAliveWithoutCalls(true)
            .permitKeepAliveTime(permitKeepAliveTime.toMillis(), TimeUnit.MILLISECONDS)
            .build();
        try {
            server.start();
            this.started = true;
            SnailJobLog.LOCAL.info("------> snail-job remoting server start success, grpc = {}, port = {}",
                SnailGrpcServer.class.getName(), snailJobProperties.getPort());
        } catch (IOException e) {
            SnailJobLog.LOCAL.error("--------> snail-job remoting server error.", e);
            started = false;
            throw new SnailJobClientException("snail-job server start error");
        }
    }

    @Override
    public void close() {
        if (server != null) {
            server.shutdownNow();
        }
    }

    private void addServices(MutableHandlerRegistry handlerRegistry, ServerInterceptor... serverInterceptor) {

        // 创建服务UNARY类型定义
        ServerServiceDefinition serviceDefinition = createUnaryServiceDefinition(
            GrpcServerConstants.UNARY_SERVICE_NAME, GrpcServerConstants.UNARY_METHOD_NAME,
            new UnaryRequestHandler(snailJobProperties.getServerRpc().getDispatcherTp(), snailDispatcherRequestHandler));
        handlerRegistry.addService(serviceDefinition);
        handlerRegistry.addService(ServerInterceptors.intercept(serviceDefinition, serverInterceptor));
    }

    public static ServerServiceDefinition createUnaryServiceDefinition(
        String serviceName,
        String methodName,
        ServerCalls.UnaryMethod<SnailJobGrpcRequest, GrpcResult> unaryMethod) {

        MethodDescriptor<SnailJobGrpcRequest, GrpcResult> methodDescriptor =
            MethodDescriptor.<SnailJobGrpcRequest, GrpcResult>newBuilder()
                .setType(MethodDescriptor.MethodType.UNARY)
                .setFullMethodName(MethodDescriptor.generateFullMethodName(serviceName, methodName))
                .setRequestMarshaller(ProtoUtils.marshaller(SnailJobGrpcRequest.getDefaultInstance()))
                .setResponseMarshaller(ProtoUtils.marshaller(GrpcResult.getDefaultInstance()))
                .build();

        return ServerServiceDefinition.builder(serviceName)
            .addMethod(methodDescriptor, ServerCalls.asyncUnaryCall(unaryMethod))
            .build();
    }

    private ThreadPoolExecutor createGrpcExecutor(final ThreadPoolConfig threadPool) {
        ThreadPoolExecutor grpcExecutor = new ThreadPoolExecutor(threadPool.getCorePoolSize(),
            threadPool.getMaximumPoolSize(), threadPool.getKeepAliveTime(), TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<>(threadPool.getQueueCapacity()),
            new ThreadFactoryBuilder().setDaemon(true).setNameFormat("snail-job-grpc-server-executor-%d")
                .build());
        grpcExecutor.allowCoreThreadTimeOut(true);
        return grpcExecutor;
    }
}
