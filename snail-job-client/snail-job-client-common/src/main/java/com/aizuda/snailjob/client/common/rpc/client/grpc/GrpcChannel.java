package com.aizuda.snailjob.client.common.rpc.client.grpc;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.aizuda.snailjob.client.common.cache.GroupVersionCache;
import com.aizuda.snailjob.client.common.config.SnailJobProperties;
import com.aizuda.snailjob.client.common.exception.SnailJobRemoteException;
import com.aizuda.snailjob.common.core.constant.SystemConstants;
import com.aizuda.snailjob.common.core.context.SnailSpringContext;
import com.aizuda.snailjob.common.core.enums.ExecutorTypeEnum;
import com.aizuda.snailjob.common.core.enums.HeadersEnum;
import com.aizuda.snailjob.common.core.grpc.auto.GrpcResult;
import com.aizuda.snailjob.common.core.grpc.auto.SnailJobGrpcRequest;
import com.aizuda.snailjob.common.core.grpc.auto.Metadata;
import com.aizuda.snailjob.common.core.util.NetUtil;
import com.aizuda.snailjob.common.core.util.SnailJobVersion;
import com.aizuda.snailjob.common.log.SnailJobLog;
import com.google.common.util.concurrent.ListenableFuture;
import io.grpc.ManagedChannel;
import io.grpc.MethodDescriptor;
import io.grpc.protobuf.ProtoUtils;
import org.springframework.boot.autoconfigure.web.ServerProperties;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author: opensnail
 * @date : 2024-08-22
 */
public final class GrpcChannel {

    private static ManagedChannel channel;
    public static void setChannel(ManagedChannel channel) {
        GrpcChannel.channel = channel;
    }

    /**
     * 服务端端口
     */
    private static final String SNAIL_JOB_SERVER_PORT = "snail-job.server.port";
    /**
     * 服务端host
     */
    private static final String SNAIL_JOB_SERVER_HOST = "snail-job.server.host";

    /**
     * 客户端端口
     */
    private static final String SNAIL_JOB_CLIENT_PORT = "snail-job.port";
    /**
     * 客户端host
     */
    private static final String SNAIL_JOB_CLIENT_HOST = "snail-job.host";

    private static final Integer MIN_PORT = 15000;
    private static final Integer MAX_PORT = 50000;
    private static final ReentrantLock PORT_LOCK = new ReentrantLock();
    private static final Integer RANDOM_CLIENT_PORT = -1;

    private static final String HOST_ID = IdUtil.getSnowflake().nextIdStr();
    private static final int PORT;
    private static final String HOST;

    static {
        PORT = Integer.parseInt(System.getProperty(SNAIL_JOB_CLIENT_PORT, String.valueOf(1789)));
        HOST = System.getProperty(SNAIL_JOB_CLIENT_HOST, NetUtil.getLocalIpStr());
    }

    /**
     * 获取服务端端口
     *
     * @return port
     */
    public static int getServerPort() {
        SnailJobProperties snailJobProperties = SnailSpringContext.getContext().getBean(SnailJobProperties.class);
        SnailJobProperties.ServerConfig serverConfig = snailJobProperties.getServer();

        String port = System.getProperty(SNAIL_JOB_SERVER_PORT);
        if (StrUtil.isBlank(port)) {
            System.setProperty(SNAIL_JOB_SERVER_PORT, String.valueOf(serverConfig.getPort()));
        }

        return Integer.parseInt(System.getProperty(SNAIL_JOB_SERVER_PORT));
    }

    /**
     * 获取服务端host
     *
     * @return host
     */
    public static String getServerHost() {
        SnailJobProperties snailJobProperties = SnailSpringContext.getBean(SnailJobProperties.class);
        SnailJobProperties.ServerConfig serverConfig = snailJobProperties.getServer();

        String host = System.getProperty(SNAIL_JOB_SERVER_HOST);
        if (StrUtil.isBlank(host)) {
            System.setProperty(SNAIL_JOB_SERVER_HOST, serverConfig.getHost());
        }

        return System.getProperty(SNAIL_JOB_SERVER_HOST);
    }

    /**
     * 获取指定的客户IP
     *
     * @return 客户端IP
     */
    public static String getClientHost() {
        SnailJobProperties snailJobProperties = SnailSpringContext.getBean(SnailJobProperties.class);

        String host = snailJobProperties.getHost();
        // 获取客户端指定的IP地址
        if (StrUtil.isBlank(host)) {
            host = HOST;
        }

        return host;
    }

    /**
     * 获取客户端端口
     *
     * @return port 端口
     */
    public static Integer getClientPort() {
        SnailJobProperties snailJobProperties = SnailSpringContext.getBean(SnailJobProperties.class);
        ServerProperties serverProperties = SnailSpringContext.getBean(ServerProperties.class);

        Integer port = snailJobProperties.getPort();
        // 获取客户端指定的端口
        if (Objects.isNull(port)) {
            port = Optional.ofNullable(serverProperties.getPort()).orElse(PORT);
            snailJobProperties.setPort(port);
            SnailJobLog.LOCAL.info("snail job client port :{}", port);
        } else if (port.equals(RANDOM_CLIENT_PORT)) {
            // 使用随机算法获取端口
            PORT_LOCK.lock();
            try {
                // 双重检查，避免重复获取端口
                if (snailJobProperties.getPort().equals(RANDOM_CLIENT_PORT)) {
                    port = getAvailablePort();
                    snailJobProperties.setPort(port);
                    SnailJobLog.LOCAL.info("snail job client port :{}", port);
                } else {
                    port = snailJobProperties.getPort();
                }
            } finally {
                PORT_LOCK.unlock();
            }
        }

        return port;
    }


    /**
     * 获取随机可用的端口
     *
     * @return 可用端口号
     */
    private static Integer getAvailablePort() {
        int port;
        do {
            port = MIN_PORT + (int) (Math.random()*(MAX_PORT - MIN_PORT));
        }while (!isPortAvailable(port));

        return port;
    }

    /**
     * 检查端口是否可以使用
     *
     * @param port 端口号
     * @return 是否可用
     */
    private static boolean isPortAvailable(int port) {
        try (ServerSocket serverSocket = new ServerSocket()) {
            // 设置端口重用
            serverSocket.setReuseAddress(true);
            // 绑定端口
            serverSocket.bind(new InetSocketAddress(port));
            return true;
        } catch (IOException e) {
            return false;
        }
    }


    public static ListenableFuture<GrpcResult> sendOfUnary(String path, String body, long reqId, Map<String, String> map) {
        if (channel == null) {
            return null;
        }

        SnailJobProperties snailJobProperties = SnailSpringContext.getBean(SnailJobProperties.class);

        // server配置不能为空
        SnailJobProperties.ServerConfig serverConfig = snailJobProperties.getServer();
        if (Objects.isNull(serverConfig)) {
            SnailJobLog.LOCAL.error("snail job server config is null");
            return null;
        }

        Assert.notBlank(snailJobProperties.getGroup(),
            () -> new SnailJobRemoteException("The group is null, please check if your configuration is correct."));

        Map<String, String> headersMap = new HashMap<>();

        headersMap.put(HeadersEnum.HOST_ID.getKey(), HOST_ID);
        headersMap.put(HeadersEnum.HOST_IP.getKey(), getClientHost());
        headersMap.put(HeadersEnum.GROUP_NAME.getKey(), snailJobProperties.getGroup());
        headersMap.put(HeadersEnum.HOST_PORT.getKey(), String.valueOf(getClientPort()));
        headersMap.put(HeadersEnum.VERSION.getKey(), String.valueOf(GroupVersionCache.getVersion()));
        headersMap.put(HeadersEnum.HOST.getKey(), serverConfig.getHost());
        headersMap.put(HeadersEnum.NAMESPACE.getKey(), Optional.ofNullable(snailJobProperties.getNamespace()).orElse(
            SystemConstants.DEFAULT_NAMESPACE));
        headersMap.put(HeadersEnum.TOKEN.getKey(), Optional.ofNullable(snailJobProperties.getToken()).orElse(
            SystemConstants.DEFAULT_TOKEN));
        headersMap.put(HeadersEnum.SYSTEM_VERSION.getKey(), Optional.ofNullable(SnailJobVersion.getVersion()).orElse(
                SystemConstants.DEFAULT_CLIENT_VERSION));
        headersMap.put(HeadersEnum.EXECUTOR_TYPE.getKey(), String.valueOf(ExecutorTypeEnum.JAVA.getType()));
        if (CollUtil.isNotEmpty(map)) {
            headersMap.putAll(map);
        }

        Metadata metadata = Metadata
            .newBuilder()
            .setUri(path)
            .putAllHeaders(headersMap)
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

}
