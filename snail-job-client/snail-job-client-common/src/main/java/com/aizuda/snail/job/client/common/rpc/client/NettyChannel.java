package com.aizuda.snail.job.client.common.rpc.client;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.aizuda.snail.job.client.common.cache.GroupVersionCache;
import com.aizuda.snail.job.client.common.config.SnailJobProperties;
import com.aizuda.snail.job.common.core.constant.SystemConstants;
import com.aizuda.snail.job.common.core.context.SpringContext;
import com.aizuda.snail.job.common.core.enums.HeadersEnum;
import com.aizuda.snail.job.common.log.EasyRetryLog;
import com.aizuda.snail.job.common.core.util.NetUtil;
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
 * @author opensnail
 * @date 2023-05-13
 * @since 1.3.0
 */
@Slf4j
public class NettyChannel {
    private NettyChannel() {
    }

    private static Channel CHANNEL;

    /**
     * 服务端端口
     */
    private static final String EASY_RETRY_SERVER_PORT = "snail-job.server.port";
    /**
     * 服务端host
     */
    private static final String EASY_RETRY_SERVER_HOST = "snail-job.server.host";

    /**
     * 客户端端口
     */
    private static final String EASY_RETRY_CLIENT_PORT = "snail-job.port";
    /**
     * 客户端host
     */
    private static final String EASY_RETRY_CLIENT_HOST = "snail-job.host";

    private static final String HOST_ID = IdUtil.getSnowflake().nextIdStr();
    private static final int PORT;
    private static final String HOST;

    static {
        PORT = Integer.parseInt(System.getProperty(EASY_RETRY_CLIENT_PORT, String.valueOf(8080)));
        HOST = System.getProperty(EASY_RETRY_CLIENT_HOST, NetUtil.getLocalIpStr());
    }

    /**
     * 获取服务端端口
     *
     * @return port
     */
    public static int getServerPort() {
        SnailJobProperties snailJobProperties = SpringContext.getContext().getBean(SnailJobProperties.class);
        SnailJobProperties.ServerConfig serverConfig = snailJobProperties.getServer();

        String port = System.getProperty(EASY_RETRY_SERVER_PORT);
        if (StrUtil.isBlank(port)) {
            System.setProperty(EASY_RETRY_SERVER_PORT, String.valueOf(serverConfig.getPort()));
        }

        return Integer.parseInt(System.getProperty(EASY_RETRY_SERVER_PORT));
    }

    /**
     * 获取服务端host
     *
     * @return host
     */
    public static String getServerHost() {
        SnailJobProperties snailJobProperties = SpringContext.getBean(SnailJobProperties.class);
        SnailJobProperties.ServerConfig serverConfig = snailJobProperties.getServer();

        String host = System.getProperty(EASY_RETRY_SERVER_HOST);
        if (StrUtil.isBlank(host)) {
            System.setProperty(EASY_RETRY_SERVER_HOST, serverConfig.getHost());
        }

        return System.getProperty(EASY_RETRY_SERVER_HOST);
    }

    /**
     * 获取指定的客户IP
     *
     * @return 客户端IP
     */
    public static String getClientHost() {
        SnailJobProperties snailJobProperties = SpringContext.getBean(SnailJobProperties.class);

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
        SnailJobProperties snailJobProperties = SpringContext.getBean(SnailJobProperties.class);
        ServerProperties serverProperties = SpringContext.getBean(ServerProperties.class);

        Integer port = snailJobProperties.getPort();
        // 获取客户端指定的端口
        if (Objects.isNull(port)) {
            port = Optional.ofNullable(serverProperties.getPort()).orElse(PORT);
        }

        return port;
    }


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

        SnailJobProperties snailJobProperties = SpringContext.getBean(SnailJobProperties.class);

        // server配置不能为空
        SnailJobProperties.ServerConfig serverConfig = snailJobProperties.getServer();
        if (Objects.isNull(serverConfig)) {
            EasyRetryLog.LOCAL.error("easy retry server config is null");
            return;
        }


        request.headers()
                .set(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON)
                //开启长连接
                .set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE)
                //设置传递请求内容的长度
                .set(HttpHeaderNames.CONTENT_LENGTH, request.content().readableBytes())
                .set(HeadersEnum.HOST_ID.getKey(), HOST_ID)
                .set(HeadersEnum.HOST_IP.getKey(), getClientHost())
                .set(HeadersEnum.GROUP_NAME.getKey(), SnailJobProperties.getGroup())
                .set(HeadersEnum.HOST_PORT.getKey(), getClientPort())
                .set(HeadersEnum.VERSION.getKey(), GroupVersionCache.getVersion())
                .set(HeadersEnum.HOST.getKey(), serverConfig.getHost())
                .set(HeadersEnum.NAMESPACE.getKey(), Optional.ofNullable(snailJobProperties.getNamespace()).orElse(
                    SystemConstants.DEFAULT_NAMESPACE))
                .set(HeadersEnum.TOKEN.getKey(), Optional.ofNullable(snailJobProperties.getToken()).orElse(
                        SystemConstants.DEFAULT_TOKEN))
        ;

        //发送数据
        CHANNEL.writeAndFlush(request).sync();
    }
}