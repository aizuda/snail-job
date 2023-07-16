package com.aizuda.easy.retry.client.core.client.netty;

import cn.hutool.core.util.IdUtil;
import com.aizuda.easy.retry.client.core.cache.GroupVersionCache;
import com.aizuda.easy.retry.client.core.config.EasyRetryProperties;
import com.aizuda.easy.retry.common.core.context.SpringContext;
import com.aizuda.easy.retry.common.core.enums.HeadersEnum;
import com.aizuda.easy.retry.common.core.log.LogUtils;
import com.aizuda.easy.retry.common.core.util.HostUtils;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.web.ServerProperties;

import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Optional;

/**
 * @author www.byteblogs.com
 * @date 2023-05-13
 * @since 1.3.0
 */
@Slf4j
public class NettyChannel {

    private static final String HOST_ID = IdUtil.simpleUUID();
    private static final String HOST_IP = HostUtils.getIp();

    private static Channel CHANNEL;

    public static void setChannel(Channel channel) {
        NettyChannel.CHANNEL = channel;
    }

    /**
     * 发送数据
     *
     * @param method 请求方式
     * @param url    url地址
     * @param body   请求的消息体
     * @throws InterruptedException
     */
    public static void send(HttpMethod method, String url, String body) throws InterruptedException {

        if (Objects.isNull(CHANNEL)) {
            LogUtils.error(log, "send message but channel is null url:[{}] method:[{}] body:[{}] ", url, method, body);
            return;
        }

        // 配置HttpRequest的请求数据和一些配置信息
        FullHttpRequest request = new DefaultFullHttpRequest(
                HttpVersion.HTTP_1_1, method, url, Unpooled.wrappedBuffer(body.getBytes(StandardCharsets.UTF_8)));

        ServerProperties serverProperties = SpringContext.CONTEXT.getBean(ServerProperties.class);

        request.headers()
                .set(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON)
                //开启长连接
                .set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE)
                //设置传递请求内容的长度
                .set(HttpHeaderNames.CONTENT_LENGTH, request.content().readableBytes())
                .set(HeadersEnum.HOST_ID.getKey(), HOST_ID)
                .set(HeadersEnum.HOST_IP.getKey(), HOST_IP)
                .set(HeadersEnum.GROUP_NAME.getKey(), EasyRetryProperties.getGroup())
                .set(HeadersEnum.CONTEXT_PATH.getKey(), Optional.ofNullable(serverProperties.getServlet().getContextPath()).orElse("/"))
                .set(HeadersEnum.HOST_PORT.getKey(), Optional.ofNullable(serverProperties.getPort()).orElse(8080))
                .set(HeadersEnum.VERSION.getKey(), GroupVersionCache.getVersion())
        ;

        //发送数据
        CHANNEL.writeAndFlush(request).sync();
    }
}
