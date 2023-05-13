package com.aizuda.easy.retry.client.core.client.netty;

import com.aizuda.easy.retry.client.core.config.EasyRetryProperties;
import com.aizuda.easy.retry.client.core.Lifecycle;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.net.ConnectException;
import java.nio.channels.ClosedChannelException;
import java.util.concurrent.TimeUnit;

/**
 * Netty 客户端
 *
 * @author: www.byteblogs.com
 * @date : 2022-03-07 18:24
 * @since 1.0.0
 */
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class NettyHttpConnectClient implements Lifecycle, ApplicationContextAware {

    private ApplicationContext applicationContext;
    private static NioEventLoopGroup nioEventLoopGroup = new NioEventLoopGroup();
    private static Bootstrap bootstrap = new Bootstrap();
    private volatile Channel channel;

    @Override
    public void start() {

        try {
            EasyRetryProperties easyRetryProperties = applicationContext.getBean(EasyRetryProperties.class);

            EasyRetryProperties.ServerConfig server = easyRetryProperties.getServer();
            final NettyHttpConnectClient thisClient = this;
            bootstrap.group(nioEventLoopGroup)
                    .channel(NioSocketChannel.class)
                    .remoteAddress(server.getHost(), server.getPort())
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel channel) throws Exception {
                            channel.pipeline()
                                    .addLast(new IdleStateHandler(0, 0, 30, TimeUnit.SECONDS))
                                    .addLast(new HttpClientCodec())
                                    .addLast(new HttpObjectAggregator(5 * 1024 * 1024))
                                    .addLast(new NettyHttpClientHandler(thisClient));
                        }
                    })
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000);

            // 开启连接服务端
            connect();
        } catch (Exception e) {
            log.error("Client start exception", e);
        }
    }

    /**
     * 连接客户端
     *
     * @return
     */
    public void connect() {

        try {
            ChannelFuture channelFuture = bootstrap.connect();

            boolean notTimeout = channelFuture.awaitUninterruptibly(30, TimeUnit.SECONDS);
            channel = channelFuture.channel();
            if (notTimeout) {
                // 连接成功
                if (channel != null && channel.isActive()) {
                    log.info("netty client started {} connect to server", channel.localAddress());
                    NettyChannel.setChannel(getChannel());
                    return;
                }

                Throwable cause = channelFuture.cause();
                if (cause != null) {
                    exceptionHandler(cause);
                }
            } else {
                log.warn("connect remote host[{}] timeout {}s", channel.remoteAddress(), 30);
            }
        } catch (Exception e) {
            exceptionHandler(e);
        }

        channel.close();

    }

    /**
     * 重连
     */
    public void reconnect() {
        ChannelFuture channelFuture = bootstrap.connect();
        channelFuture.addListener((ChannelFutureListener) future -> {
            Throwable cause = future.cause();
            if (cause != null) {
                exceptionHandler(cause);
            } else {
                channel = channelFuture.channel();
                if (channel != null && channel.isActive()) {
                    log.info("Netty client {} reconnect to server", channel.localAddress());
                    NettyChannel.setChannel(getChannel());
                }
            }
        });
    }

    /**
     * 连接失败处理
     *
     * @param cause
     */
    private void exceptionHandler(Throwable cause) {
        if (cause instanceof ConnectException) {
            log.error("connect error:{}", cause.getMessage());
        } else if (cause instanceof ClosedChannelException) {
            log.error("connect error:{}", "client has destroy");
        } else {
            log.error("connect error:", cause);
        }
    }


    @Override
    public void close() {
        if (channel != null) {
            channel.close();
        }
        if (nioEventLoopGroup != null) {
            nioEventLoopGroup.shutdownGracefully();
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    public Channel getChannel() {
        return channel;
    }
}
