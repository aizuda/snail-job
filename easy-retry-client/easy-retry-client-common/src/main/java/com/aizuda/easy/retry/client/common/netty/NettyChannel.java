package com.aizuda.easy.retry.client.common.netty;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.aizuda.easy.retry.client.common.cache.GroupVersionCache;
import com.aizuda.easy.retry.client.common.config.EasyRetryProperties;
import com.aizuda.easy.retry.common.core.constant.SystemConstants;
import com.aizuda.easy.retry.common.core.context.SpringContext;
import com.aizuda.easy.retry.common.core.enums.HeadersEnum;
import com.aizuda.easy.retry.common.log.EasyRetryLog;
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

    private NettyChannel() {
    }

    private static final String HOST_ID = IdUtil.getSnowflake().nextIdStr();
    private static final int PORT;
    private static final String HOST;

    static {
        PORT = Integer.parseInt(System.getProperty("easy-retry.port", String.valueOf(8080)));
        HOST = System.getProperty("easy-retry.host", HostUtils.getIp());
    }

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
            EasyRetryLog.LOCAL.error("send message but channel is null url:[{}] method:[{}] body:[{}] ", url, method, body);
            return;
        }

        // 配置HttpRequest的请求数据和一些配置信息
        FullHttpRequest request = new DefaultFullHttpRequest(
                HttpVersion.HTTP_1_1, method, url, Unpooled.wrappedBuffer(body.getBytes(StandardCharsets.UTF_8)));

        ServerProperties serverProperties = SpringContext.CONTEXT.getBean(ServerProperties.class);
        EasyRetryProperties easyRetryProperties = SpringContext.CONTEXT.getBean(EasyRetryProperties.class);

        // server配置不能为空
        EasyRetryProperties.ServerConfig serverConfig = easyRetryProperties.getServer();
        if (Objects.isNull(serverConfig)) {
            EasyRetryLog.LOCAL.error("easy retry server config is null");
            return;
        }

        Integer port = easyRetryProperties.getPort();
        // 获取客户端指定的端口
        if (Objects.isNull(port)) {
            port = Optional.ofNullable(serverProperties.getPort()).orElse(PORT);
        }

        String host = easyRetryProperties.getHost();
        // 获取客户端指定的IP地址
        if (StrUtil.isBlank(host)) {
            host = HOST;
        }

        request.headers()
                .set(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON)
                //开启长连接
                .set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE)
                //设置传递请求内容的长度
                .set(HttpHeaderNames.CONTENT_LENGTH, request.content().readableBytes())
                .set(HeadersEnum.HOST_ID.getKey(), HOST_ID)
                .set(HeadersEnum.HOST_IP.getKey(), host)
                .set(HeadersEnum.GROUP_NAME.getKey(), EasyRetryProperties.getGroup())
                .set(HeadersEnum.CONTEXT_PATH.getKey(), Optional.ofNullable(serverProperties.getServlet().getContextPath()).orElse("/"))
                .set(HeadersEnum.HOST_PORT.getKey(), port)
                .set(HeadersEnum.VERSION.getKey(), GroupVersionCache.getVersion())
                .set(HeadersEnum.HOST.getKey(), serverConfig.getHost())
                .set(HeadersEnum.NAMESPACE.getKey(), Optional.ofNullable(easyRetryProperties.getNamespace()).orElse(
                    SystemConstants.DEFAULT_NAMESPACE))
        ;

        //发送数据
        CHANNEL.writeAndFlush(request).sync();
    }
}
