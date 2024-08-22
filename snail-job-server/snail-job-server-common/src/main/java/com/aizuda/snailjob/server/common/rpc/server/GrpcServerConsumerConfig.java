package com.aizuda.snailjob.server.common.rpc.server;

import com.aizuda.snailjob.common.core.grpc.auto.GrpcResult;
import com.aizuda.snailjob.common.core.grpc.auto.GrpcSnailJobRequest;
import io.grpc.MethodDescriptor;
import io.grpc.ServerBuilder;
import io.grpc.ServerCallHandler;
import io.grpc.ServerServiceDefinition;
import io.grpc.protobuf.ProtoUtils;
import io.grpc.stub.ServerCalls;
import net.devh.boot.grpc.server.serverfactory.GrpcServerConfigurer;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GrpcServerConsumerConfig implements GrpcServerConfigurer {

    @Override
    public void accept(ServerBuilder<?> serverBuilder) {
        // 创建服务定义
        ServerServiceDefinition serviceDefinition = createServiceDefinition(
            "UnaryRequest", "unaryRequest",
            new UnaryRequestHandler());
        serverBuilder.addService(serviceDefinition);
        serverBuilder.intercept(new GrpcInterceptor());
    }

    public static  ServerServiceDefinition createServiceDefinition(
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
