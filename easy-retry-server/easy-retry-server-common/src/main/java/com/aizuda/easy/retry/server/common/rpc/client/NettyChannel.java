package com.aizuda.easy.retry.server.common.rpc.client;

import com.aizuda.easy.retry.common.log.EasyRetryLog;
import com.aizuda.easy.retry.server.common.triple.Pair;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;
import lombok.extern.slf4j.Slf4j;

import java.net.ConnectException;
import java.nio.channels.ClosedChannelException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * @author opensnail
 * @date 2023-05-13
 * @since 1.3.0
 */
@Slf4j
public class NettyChannel {
    private NettyChannel() {
    }

    private static Bootstrap bootstrap;
    private static ConcurrentHashMap<Pair<String, String>, Channel> CHANNEL_MAP = new ConcurrentHashMap<>(16);


    public static void setChannel(String hostId, String ip, Channel channel) {
        CHANNEL_MAP.put(Pair.of(hostId, ip), channel);
    }

    public static void removeChannel(Channel channel) {
        CHANNEL_MAP.forEach((key, value) -> {
            if (value.equals(channel)) {
                CHANNEL_MAP.remove(key);
            }
        });
    }

    public static void setBootstrap(Bootstrap bootstrap) {
        NettyChannel.bootstrap = bootstrap;
    }

    /**
     * 发送数据
     *
     * @param method 请求方式
     * @param url    url地址
     * @param body   请求的消息体
     * @throws InterruptedException
     */
    public static void send(String hostId, String hostIp, Integer port, HttpMethod method, String url, String body, HttpHeaders requestHeaders) throws InterruptedException {

        Channel channel = CHANNEL_MAP.get(Pair.of(hostId, hostIp));
        if (Objects.isNull(channel) || !channel.isActive()) {
            channel = connect(hostId, hostIp, port);
            if (Objects.isNull(channel)) {
                EasyRetryLog.LOCAL.error("send message but channel is null url:[{}] method:[{}] body:[{}] ", url, method, body);
                return;
            }
        }

        // 配置HttpRequest的请求数据和一些配置信息
        FullHttpRequest request = new DefaultFullHttpRequest(
                HttpVersion.HTTP_1_1, method, url, Unpooled.wrappedBuffer(body.getBytes(StandardCharsets.UTF_8)));

        request.headers()
                .set(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON)
                //开启长连接
                .set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE)
                //设置传递请求内容的长度
                .set(HttpHeaderNames.CONTENT_LENGTH, request.content().readableBytes())
        ;
        request.headers().setAll(requestHeaders);

        //发送数据
        channel.writeAndFlush(request).sync();
    }

    /**
     * 连接客户端
     *
     * @return
     */
    public static Channel connect(String hostId, String ip, Integer port) {

        try {
            ChannelFuture channelFuture = bootstrap
                .remoteAddress(ip, port)
                .connect();

            boolean notTimeout = channelFuture.awaitUninterruptibly(30, TimeUnit.SECONDS);
            Channel channel = channelFuture.channel();
            if (notTimeout) {
                // 连接成功
                if (channel != null && channel.isActive()) {
                    EasyRetryLog.LOCAL.info("netty client started {} connect to server", channel.localAddress());
                    NettyChannel.setChannel(hostId, ip, channel);
                    return channel;
                }

                Throwable cause = channelFuture.cause();
                if (cause != null) {
                    exceptionHandler(cause);
                }
            } else {
                EasyRetryLog.LOCAL.warn("connect remote host[{}] timeout {}s", channel.remoteAddress(), 30);
            }
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
            EasyRetryLog.LOCAL.error("connect error:{}", cause.getMessage());
        } else if (cause instanceof ClosedChannelException) {
            EasyRetryLog.LOCAL.error("connect error:{}", "client has destroy");
        } else {
            EasyRetryLog.LOCAL.error("connect error:", cause);
        }
    }

}
