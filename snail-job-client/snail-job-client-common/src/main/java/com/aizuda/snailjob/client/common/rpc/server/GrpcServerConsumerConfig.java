package com.aizuda.snailjob.client.common.rpc.server;

import com.aizuda.snailjob.client.common.config.SnailJobProperties;
import com.aizuda.snailjob.client.common.config.SnailJobProperties.DispatcherThreadPool;
import com.aizuda.snailjob.client.common.rpc.supports.handler.SnailDispatcherRequestHandler;
import com.aizuda.snailjob.client.common.rpc.supports.handler.UnaryRequestHandler;
import com.aizuda.snailjob.common.core.grpc.auto.GrpcResult;
import com.aizuda.snailjob.common.core.grpc.auto.GrpcSnailJobRequest;
import io.grpc.MethodDescriptor;
import io.grpc.ServerBuilder;
import io.grpc.ServerCallHandler;
import io.grpc.ServerServiceDefinition;
import io.grpc.protobuf.ProtoUtils;
import io.grpc.stub.ServerCalls;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.serverfactory.GrpcServerConfigurer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
@RequiredArgsConstructor
public class GrpcServerConsumerConfig implements GrpcServerConfigurer {
    private final SnailDispatcherRequestHandler handler;
    private final SnailJobProperties snailJobProperties;

    @Override
    public void accept(ServerBuilder<?> serverBuilder) {
        DispatcherThreadPool threadPool = snailJobProperties.getDispatcherThreadPool();
        ThreadPoolExecutor dispatcherThreadPool = new ThreadPoolExecutor(
            threadPool.getCorePoolSize(), threadPool.getMaximumPoolSize(), threadPool.getKeepAliveTime(),
            threadPool.getTimeUnit(), new LinkedBlockingQueue<>(threadPool.getQueueCapacity()),
            new CustomizableThreadFactory("snail-grpc-server-"));

        // 创建服务定义
        ServerServiceDefinition serviceDefinition = createServiceDefinition(
            "UnaryRequest", "unaryRequest",
            new UnaryRequestHandler(dispatcherThreadPool, handler));
        serverBuilder.addService(serviceDefinition);
        serverBuilder.intercept(new GrpcInterceptor());
    }

    public static ServerServiceDefinition createServiceDefinition(
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

        ServerCallHandler<GrpcSnailJobRequest, GrpcResult> callHandler = ServerCalls.asyncUnaryCall(unaryMethod);

        return ServerServiceDefinition.builder(serviceName)
            .addMethod(methodDescriptor, callHandler)
            .build();
    }
}
