//package com.aizuda.snailjob.client.common.rpc.client;
//
//import com.aizuda.snailjob.common.core.grpc.auto.GrpcResult;
//import com.aizuda.snailjob.common.core.grpc.auto.GrpcSnailJobRequest;
//import com.aizuda.snailjob.common.core.grpc.auto.Metadata;
//import com.google.common.util.concurrent.FutureCallback;
//import com.google.common.util.concurrent.Futures;
//import com.google.common.util.concurrent.ListenableFuture;
//import com.google.protobuf.Any;
//import com.google.protobuf.UnsafeByteOperations;
//import io.grpc.Channel;
//import io.grpc.ManagedChannel;
//import io.grpc.ManagedChannelBuilder;
//import io.grpc.MethodDescriptor;
//import io.grpc.protobuf.ProtoUtils;
//
//import java.util.concurrent.LinkedBlockingDeque;
//import java.util.concurrent.ScheduledThreadPoolExecutor;
//import java.util.concurrent.ThreadPoolExecutor;
//import java.util.concurrent.TimeUnit;
//
///**
// * @author: shuguang.zhang
// * @date : 2024-08-21
// */
//public class GrpcClient2 {
//    private final Channel channel;
//
//    public GrpcClient2(Channel channel) {
//        this.channel = channel;
//    }
//
//    public static void main(String[] args) {
//        // 创建 gRPC 频道
//        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 1788)
//            .usePlaintext()
//            .build();
//
//        // 实例化客户端
//       GrpcClient2 grpcClient = new GrpcClient2(channel);
//
//        String s = "zsg";
//        Any build = Any.newBuilder().setValue(UnsafeByteOperations.unsafeWrap(s.getBytes())).build();
//        // 构造请求对象
//        Metadata metadata = Metadata
//            .newBuilder()
//            .setClientIp("11")
//            .setType("1")
//            .putHeaders("aa", "bb")
//            .build();
//        GrpcSnailJobRequest request = GrpcSnailJobRequest.newBuilder().setMetadata(metadata).setBody(build).build();
//
//        // 动态调用方法并获取响应 "UnaryRequest", "unaryRequest",
//        ListenableFuture<GrpcResult> future = grpcClient.invokeMethod("unaryRequest", request);
//        ScheduledThreadPoolExecutor scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(1);
//        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(2,2,1,TimeUnit.SECONDS, new LinkedBlockingDeque<>());
////        System.out.println("Response: " + response.getMessage());
//        Futures.addCallback(future, new FutureCallback<GrpcResult>() {
//            @Override
//            public void onSuccess(final GrpcResult result) {
//                System.out.println(result);
//            }
//
//            @Override
//            public void onFailure(final Throwable t) {
//                System.out.println(t);
//            }
//        }, threadPoolExecutor);
//
//        Futures.withTimeout(future, 5, TimeUnit.SECONDS, scheduledThreadPoolExecutor);
//        // 关闭频道
//        channel.shutdown();
//    }
//
//    public ListenableFuture<GrpcResult> invokeMethod(String methodName, GrpcSnailJobRequest request) {
//        MethodDescriptor<GrpcSnailJobRequest, GrpcResult> methodDescriptor =
//            MethodDescriptor.<GrpcSnailJobRequest, GrpcResult>newBuilder()
//                .setType(MethodDescriptor.MethodType.UNARY)
//                .setFullMethodName(MethodDescriptor.generateFullMethodName("UnaryRequest", methodName))
//                .setRequestMarshaller(ProtoUtils.marshaller(GrpcSnailJobRequest.getDefaultInstance()))
//                .setResponseMarshaller(ProtoUtils.marshaller(GrpcResult.getDefaultInstance()))
//                .build();
//
//        // 创建动态代理调用方法
//        return io.grpc.stub.ClientCalls.futureUnaryCall(
//            channel.newCall(methodDescriptor, io.grpc.CallOptions.DEFAULT),
//            request);
//    }
//}
