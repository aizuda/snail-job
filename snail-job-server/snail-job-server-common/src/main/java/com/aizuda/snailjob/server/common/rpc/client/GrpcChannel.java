package com.aizuda.snailjob.server.common.rpc.client;

import com.aizuda.snailjob.common.core.grpc.auto.GrpcResult;
import com.aizuda.snailjob.common.core.grpc.auto.GrpcSnailJobRequest;
import com.aizuda.snailjob.common.core.grpc.auto.Metadata;
import com.aizuda.snailjob.common.log.SnailJobLog;
import com.aizuda.snailjob.server.common.triple.Pair;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.protobuf.Any;
import com.google.protobuf.UnsafeByteOperations;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.MethodDescriptor;
import io.grpc.protobuf.ProtoUtils;
import io.netty.bootstrap.Bootstrap;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import lombok.extern.slf4j.Slf4j;

import java.net.ConnectException;
import java.nio.channels.ClosedChannelException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author opensnail
 * @date 2023-05-13
 * @since 1.3.0
 */
@Slf4j
public class GrpcChannel {
    private GrpcChannel() {
    }

    private static ConcurrentHashMap<Pair<String, String>, ManagedChannel> CHANNEL_MAP = new ConcurrentHashMap<>(16);

    public static void setChannel(String hostId, String ip, ManagedChannel channel) {
        CHANNEL_MAP.put(Pair.of(hostId, ip), channel);
    }

    public static void removeChannel(ManagedChannel channel) {
        CHANNEL_MAP.forEach((key, value) -> {
            if (value.equals(channel)) {
                CHANNEL_MAP.remove(key);
            }
        });
    }


    /**
     * 发送数据
     *
     * @param url    url地址
     * @param body   请求的消息体
     * @throws InterruptedException
     */
    public static ListenableFuture<GrpcResult> send(String hostId, String hostIp, Integer port, String url, String body, Map<String, String> headersMap) throws InterruptedException {

        ManagedChannel channel = CHANNEL_MAP.get(Pair.of(hostId, hostIp));
        if (Objects.isNull(channel) || !channel.isShutdown() || !channel.isShutdown()) {
            channel = connect(hostId, hostIp, port);
            if (Objects.isNull(channel)) {
                SnailJobLog.LOCAL.error("send message but channel is null url:[{}] method:[{}] body:[{}] ", url, body);
                return null;
            }
        }

        Metadata metadata = Metadata
            .newBuilder()
            .setUri(url)
            .putAllHeaders(headersMap)
            .build();
        Any build = Any.newBuilder().setValue(UnsafeByteOperations.unsafeWrap(body.getBytes()))
            .build();
        GrpcSnailJobRequest snailJobRequest = GrpcSnailJobRequest
            .newBuilder()
            .setMetadata(metadata)
            .setBody(build)
            .build();

        MethodDescriptor<GrpcSnailJobRequest, GrpcResult> methodDescriptor =
            MethodDescriptor.<GrpcSnailJobRequest, GrpcResult>newBuilder()
                .setType(MethodDescriptor.MethodType.UNARY)
                .setFullMethodName(MethodDescriptor.generateFullMethodName("UnaryRequest", "unaryRequest"))
                .setRequestMarshaller(ProtoUtils.marshaller(GrpcSnailJobRequest.getDefaultInstance()))
                .setResponseMarshaller(ProtoUtils.marshaller(GrpcResult.getDefaultInstance()))
                .build();

        // 创建动态代理调用方法
        return io.grpc.stub.ClientCalls.futureUnaryCall(
            channel.newCall(methodDescriptor, io.grpc.CallOptions.DEFAULT),
            snailJobRequest);

    }

    /**
     * 连接客户端
     *
     * @return
     */
    public static ManagedChannel connect(String hostId, String ip, Integer port) {

        try {
            ManagedChannel channel = ManagedChannelBuilder.forAddress(ip, port)
                .usePlaintext()
                .build();
            GrpcChannel.setChannel(hostId, ip, channel);

            return channel;
        } catch (Exception e) {
            exceptionHandler(e);
        }

        return null;
    }

    /**
     * 连接失败处理
     *
     * @param cause
     */
    private static void exceptionHandler(Throwable cause) {
        if (cause instanceof ConnectException) {
            SnailJobLog.LOCAL.error("connect error:{}", cause.getMessage());
        } else if (cause instanceof ClosedChannelException) {
            SnailJobLog.LOCAL.error("connect error:{}", "client has destroy");
        } else {
            SnailJobLog.LOCAL.error("connect error:", cause);
        }
    }

}
