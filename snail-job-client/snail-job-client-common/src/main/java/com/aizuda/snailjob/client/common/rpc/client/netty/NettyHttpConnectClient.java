package com.aizuda.snailjob.client.common.rpc.client.netty;

import com.aizuda.snailjob.client.common.Lifecycle;
import com.aizuda.snailjob.client.common.config.SnailJobProperties;
import com.aizuda.snailjob.client.common.handler.ClientRegister;
import com.aizuda.snailjob.common.core.enums.RpcTypeEnum;
import com.aizuda.snailjob.common.log.SnailJobLog;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.net.ConnectException;
import java.nio.channels.ClosedChannelException;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * Netty 客户端（下线）
 *
 * @author: opensnail
 * @date : 2022-03-07 18:24
 * @since 1.0.0
 */
@Getter
//@Component
@RequiredArgsConstructor
@Order(Ordered.HIGHEST_PRECEDENCE)
@Deprecated
public class NettyHttpConnectClient implements Lifecycle {
    private final SnailJobProperties snailJobProperties;
    private static final NioEventLoopGroup nioEventLoopGroup = new NioEventLoopGroup();
    private static final Bootstrap bootstrap = new Bootstrap();
    private Channel channel;

    @Override
    public void start() {

//        if (RpcTypeEnum.NETTY != snailJobProperties.getRpcType()) {
//            return;
//        }

        try {
            final NettyHttpConnectClient thisClient = this;
            bootstrap.group(nioEventLoopGroup)
                    .channel(NioSocketChannel.class)
                    .remoteAddress(NettyChannel.getServerHost(), NettyChannel.getServerPort())
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel channel) throws Exception {
                            channel.pipeline()
                                    .addLast(new IdleStateHandler(0, 0, 3 * ClientRegister.REGISTER_TIME, TimeUnit.SECONDS))
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
            SnailJobLog.LOCAL.error("Client start exception", e);
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
                    SnailJobLog.LOCAL.info("netty client started {} connect to server", channel.localAddress());
                    NettyChannel.setChannel(getChannel());
                    return;
                }

                Throwable cause = channelFuture.cause();
                if (cause != null) {
                    exceptionHandler(cause);
                }
            } else {
                SnailJobLog.LOCAL.warn("connect remote host[{}] timeout {}s", channel.remoteAddress(), 30);
            }
        } catch (Exception e) {
            exceptionHandler(e);
        }

        // 若连接失败尝试关闭改channel
        if (Objects.nonNull(channel)) {
            channel.close();
        }
    }

    /**
     * 重连
     */
    public void reconnect() {
        ChannelFuture channelFuture = bootstrap
                .remoteAddress(NettyChannel.getServerHost(), NettyChannel.getServerPort())
                .connect();
        channelFuture.addListener((ChannelFutureListener) future -> {
            Throwable cause = future.cause();
            if (cause != null) {
                exceptionHandler(cause);
            } else {
                channel = channelFuture.channel();
                if (channel != null && channel.isActive()) {
                    SnailJobLog.LOCAL.info("Netty client {} reconnect to server", channel.localAddress());
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
            SnailJobLog.LOCAL.error("connect error:{}", cause.getMessage());
        } else if (cause instanceof ClosedChannelException) {
            SnailJobLog.LOCAL.error("connect error:{}", "client has destroy");
        } else {
            SnailJobLog.LOCAL.error("connect error:", cause);
        }
    }


    @Override
    public void close() {
        if (channel != null) {
            channel.close();
        }
        nioEventLoopGroup.shutdownGracefully();
    }

}
