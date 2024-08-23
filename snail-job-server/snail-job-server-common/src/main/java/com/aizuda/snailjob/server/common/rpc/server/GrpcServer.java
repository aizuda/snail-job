package com.aizuda.snailjob.server.common.rpc.server;

import com.aizuda.snailjob.common.core.constant.GrpcServerConstants;
import com.aizuda.snailjob.common.core.enums.RpcTypeEnum;
import com.aizuda.snailjob.common.core.grpc.auto.GrpcResult;
import com.aizuda.snailjob.common.core.grpc.auto.GrpcSnailJobRequest;
import com.aizuda.snailjob.common.log.SnailJobLog;
import com.aizuda.snailjob.server.common.Lifecycle;
import com.aizuda.snailjob.server.common.config.SystemProperties;
import com.aizuda.snailjob.server.common.config.SystemProperties.RpcServerProperties;
import com.aizuda.snailjob.server.common.config.SystemProperties.ThreadPoolConfig;
import com.aizuda.snailjob.server.common.exception.SnailJobServerException;
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
 * netty server
 *
 * @author: opensnail
 * @date : 2022-03-07 15:54
 * @since 1.0.0
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
@Getter
public class GrpcServer implements Lifecycle {

    private final SystemProperties systemProperties;
    private volatile boolean started = false;
    private Server server;

    @Override
    public void start() {
        // 防止重复启动
        if (started) {
            return;
        }

        if (RpcTypeEnum.GPRC != systemProperties.getRpcType()) {
            return;
        }

        RpcServerProperties grpc = systemProperties.getServerRpc();

        final MutableHandlerRegistry handlerRegistry = new MutableHandlerRegistry();
        addServices(handlerRegistry, new GrpcInterceptor());
        NettyServerBuilder builder = NettyServerBuilder.forPort(systemProperties.getNettyPort())
            .executor(createGrpcExecutor(grpc.getDispatcherTp()));

        Duration keepAliveTime = grpc.getKeepAliveTime();
        Duration keepAliveTimeOut = grpc.getKeepAliveTimeout();
        Duration permitKeepAliveTime = grpc.getPermitKeepAliveTime();

        server = builder.maxInboundMessageSize(grpc.getMaxInboundMessageSize()).fallbackHandlerRegistry(handlerRegistry)
            .compressorRegistry(CompressorRegistry.getDefaultInstance())
            .decompressorRegistry(DecompressorRegistry.getDefaultInstance())
            .keepAliveTime(keepAliveTime.toMillis(), TimeUnit.MILLISECONDS)
            .keepAliveTimeout(keepAliveTimeOut.toMillis(), TimeUnit.MILLISECONDS)
            .permitKeepAliveTime(permitKeepAliveTime.toMillis(), TimeUnit.MILLISECONDS)
            .build();
        try {
            server.start();
            this.started = true;
            SnailJobLog.LOCAL.info("------> snail-job remoting server start success, grpc = {}, port = {}",
                GrpcServer.class.getName(), systemProperties.getNettyPort());
        } catch (IOException e) {
            SnailJobLog.LOCAL.error("--------> snail-job remoting server error.", e);
            started = false;
            throw new SnailJobServerException("snail-job server start error");
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
            new UnaryRequestHandler());
        handlerRegistry.addService(serviceDefinition);
        // unary common call register.

        handlerRegistry.addService(ServerInterceptors.intercept(serviceDefinition, serverInterceptor));
    }

    public static ServerServiceDefinition createUnaryServiceDefinition(
        String serviceName,
        String methodName,
        ServerCalls.UnaryMethod<GrpcSnailJobRequest, GrpcResult> unaryMethod) {

        MethodDescriptor<GrpcSnailJobRequest, GrpcResult> methodDescriptor =
            MethodDescriptor.<GrpcSnailJobRequest, GrpcResult>newBuilder()
                .setType(MethodDescriptor.MethodType.UNARY)
                .setFullMethodName(MethodDescriptor.generateFullMethodName(serviceName, methodName))
                .setRequestMarshaller(ProtoUtils.marshaller(GrpcSnailJobRequest.getDefaultInstance()))
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
