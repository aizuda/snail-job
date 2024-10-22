package com.aizuda.snailjob.server.common.rpc.client;

import com.aizuda.snailjob.common.core.context.SnailSpringContext;
import com.aizuda.snailjob.common.core.grpc.auto.GrpcResult;
import com.aizuda.snailjob.common.core.grpc.auto.GrpcSnailJobRequest;
import com.aizuda.snailjob.common.core.grpc.auto.Metadata;
import com.aizuda.snailjob.common.log.SnailJobLog;
import com.aizuda.snailjob.server.common.config.SystemProperties;
import com.aizuda.snailjob.server.common.config.SystemProperties.RpcClientProperties;
import com.aizuda.snailjob.server.common.config.SystemProperties.ThreadPoolConfig;
import com.aizuda.snailjob.server.common.triple.Pair;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.protobuf.Any;
import com.google.protobuf.UnsafeByteOperations;
import io.grpc.DecompressorRegistry;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.MethodDescriptor;
import io.grpc.protobuf.ProtoUtils;
import lombok.extern.slf4j.Slf4j;

import java.net.ConnectException;
import java.nio.channels.ClosedChannelException;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author opensnail
 * @date 2023-05-13
 * @since 1.3.0
 */
@Slf4j
public class GrpcChannel {
    private GrpcChannel() {
    }

    private static final ThreadPoolExecutor grpcExecutor = createGrpcExecutor();
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
     * @param url   url地址
     * @param body  请求的消息体
     * @param reqId
     * @throws InterruptedException
     */
    public static ListenableFuture<GrpcResult> send(String hostId, String hostIp, Integer port, String url, String body, Map<String, String> headersMap,
        final long reqId) {

        ManagedChannel channel = CHANNEL_MAP.get(Pair.of(hostId, hostIp));
        if (Objects.isNull(channel) || channel.isShutdown() || channel.isTerminated()) {
            removeChannel(channel);
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
        GrpcSnailJobRequest snailJobRequest = GrpcSnailJobRequest
            .newBuilder()
            .setMetadata(metadata)
            .setReqId(reqId)
            .setBody(body)
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
            RpcClientProperties clientRpc = SnailSpringContext.getBean(SystemProperties.class).getClientRpc();
            ManagedChannel channel = ManagedChannelBuilder.forAddress(ip, port)
                .executor(grpcExecutor)
                .decompressorRegistry(DecompressorRegistry.getDefaultInstance())
                .maxInboundMessageSize(clientRpc.getMaxInboundMessageSize())
                .keepAliveTime(clientRpc.getKeepAliveTime().toMillis(), TimeUnit.MILLISECONDS)
                .keepAliveTimeout(clientRpc.getKeepAliveTimeout().toMillis(), TimeUnit.MILLISECONDS)
                .usePlaintext()
                .build();
            GrpcChannel.setChannel(hostId, ip, channel);

            return channel;
        } catch (Exception e) {
            exceptionHandler(e);
        }

        return null;
    }

    private static ThreadPoolExecutor createGrpcExecutor() {
        RpcClientProperties clientRpc = SnailSpringContext.getBean(SystemProperties.class).getClientRpc();
        ThreadPoolConfig clientTp = clientRpc.getClientTp();
        ThreadPoolExecutor grpcExecutor = new ThreadPoolExecutor(clientTp.getCorePoolSize(),
            clientTp.getMaximumPoolSize(), clientTp.getKeepAliveTime(), TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<>(clientTp.getQueueCapacity()),
            new ThreadFactoryBuilder().setDaemon(true).setNameFormat("snail-job-grpc-client-executor-%d")
                .build());
        grpcExecutor.allowCoreThreadTimeOut(true);
        return grpcExecutor;
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
