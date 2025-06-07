package com.aizuda.snailjob.server.common.rpc.client.grpc;

import com.aizuda.snailjob.common.core.constant.SystemConstants;
import com.aizuda.snailjob.common.core.context.SnailSpringContext;
import com.aizuda.snailjob.common.core.enums.HeadersEnum;
import com.aizuda.snailjob.common.core.grpc.auto.GrpcResult;
import com.aizuda.snailjob.common.core.grpc.auto.SnailJobGrpcRequest;
import com.aizuda.snailjob.common.core.grpc.auto.Metadata;
import com.aizuda.snailjob.common.core.util.NetUtil;
import com.aizuda.snailjob.common.log.SnailJobLog;
import com.aizuda.snailjob.server.common.config.SystemProperties;
import com.aizuda.snailjob.server.common.config.SystemProperties.RpcClientProperties;
import com.aizuda.snailjob.server.common.config.SystemProperties.ThreadPoolConfig;
import com.aizuda.snailjob.server.common.register.ServerRegister;
import com.aizuda.snailjob.server.common.triple.Pair;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
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
    public static synchronized ListenableFuture<GrpcResult> send(String url, String body,
                                                                 Map<String, String> headers,
                                                                 long reqId,
                                                                 ManagedChannel channel) {

        headers.put(HeadersEnum.HOST_ID.getKey(), ServerRegister.CURRENT_CID);
        headers.put(HeadersEnum.HOST_IP.getKey(), NetUtil.getLocalIpStr());
        headers.put(HeadersEnum.GROUP_NAME.getKey(), ServerRegister.GROUP_NAME);
        headers.put(HeadersEnum.HOST_PORT.getKey(), getServerPort());
        headers.put(HeadersEnum.NAMESPACE.getKey(), SystemConstants.DEFAULT_NAMESPACE);
        headers.put(HeadersEnum.TOKEN.getKey(), getServerToken());

        Metadata metadata = Metadata
                .newBuilder()
                .setUri(url)
                .putAllHeaders(headers)
                .build();
        SnailJobGrpcRequest snailJobRequest = SnailJobGrpcRequest
                .newBuilder()
                .setMetadata(metadata)
                .setReqId(reqId)
                .setBody(body)
                .build();

        MethodDescriptor<SnailJobGrpcRequest, GrpcResult> methodDescriptor =
                MethodDescriptor.<SnailJobGrpcRequest, GrpcResult>newBuilder()
                        .setType(MethodDescriptor.MethodType.UNARY)
                        .setFullMethodName(MethodDescriptor.generateFullMethodName("UnaryRequest", "unaryRequest"))
                        .setRequestMarshaller(ProtoUtils.marshaller(SnailJobGrpcRequest.getDefaultInstance()))
                        .setResponseMarshaller(ProtoUtils.marshaller(GrpcResult.getDefaultInstance()))
                        .build();

        // 创建动态代理调用方法
        return io.grpc.stub.ClientCalls.futureUnaryCall(
                channel.newCall(methodDescriptor, io.grpc.CallOptions.DEFAULT),
                snailJobRequest);

    }

    private static String getServerToken() {
        SystemProperties properties = SnailSpringContext.getBean(SystemProperties.class);
        return properties.getServerToken();
    }

    private static String getServerPort() {
        SystemProperties properties = SnailSpringContext.getBean(SystemProperties.class);
        return String.valueOf(properties.getServerPort());
    }

    /**
     * 连接客户端
     *
     * @return
     */
    public static ManagedChannel connect(String ip, Integer port) {

        try {
            RpcClientProperties clientRpc = SnailSpringContext.getBean(SystemProperties.class).getClientRpc();
            return ManagedChannelBuilder.forAddress(ip, port)
                    .usePlaintext()
                    .executor(grpcExecutor)
                    .decompressorRegistry(DecompressorRegistry.getDefaultInstance())
                    .maxInboundMessageSize(clientRpc.getMaxInboundMessageSize())
                    .keepAliveTime(clientRpc.getKeepAliveTime().toMillis(), TimeUnit.MILLISECONDS)
                    .keepAliveTimeout(clientRpc.getKeepAliveTimeout().toMillis(), TimeUnit.MILLISECONDS)
                    .idleTimeout(clientRpc.getIdleTimeout().toMillis(), TimeUnit.MILLISECONDS)
                    .keepAliveWithoutCalls(false)
                    .build();
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
