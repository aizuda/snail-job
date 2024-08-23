package com.aizuda.snailjob.client.common.rpc.server;

import com.aizuda.snailjob.client.common.Lifecycle;
import com.aizuda.snailjob.client.common.config.SnailJobProperties;
import com.aizuda.snailjob.client.common.event.SnailServerStartFailedEvent;
import com.aizuda.snailjob.client.common.exception.SnailJobClientException;
import com.aizuda.snailjob.client.common.rpc.supports.handler.NettyHttpServerHandler;
import com.aizuda.snailjob.client.common.rpc.supports.handler.SnailDispatcherRequestHandler;
import com.aizuda.snailjob.common.core.context.SnailSpringContext;
import com.aizuda.snailjob.common.core.enums.RpcTypeEnum;
import com.aizuda.snailjob.common.log.SnailJobLog;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * netty server
 *
 * @author: opensnail
 * @date : 2024-04-12 23:03
 * @since 3.3.0
 */
//@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
@Getter
public class SnailNettyHttpServer implements Runnable, Lifecycle {
    private final SnailJobProperties snailJobProperties;
    private final SnailDispatcherRequestHandler snailDispatcherRequestHandler;
    private Thread thread = null;
    private volatile boolean started = false;

    @Override
    public void run() {
        // 防止重复启动
        if (started) {
            return;
        }

        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            // start server
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel channel) throws Exception {
                            channel.pipeline()
                                    .addLast(new HttpServerCodec())
                                    .addLast(new HttpObjectAggregator(5 * 1024 * 1024))
                                    .addLast(new NettyHttpServerHandler(snailDispatcherRequestHandler, snailJobProperties));
                        }
                    });

            // 在特定端口绑定并启动服务器 默认是1789
            ChannelFuture future = bootstrap.bind(snailJobProperties.getPort()).sync();

            SnailJobLog.LOCAL.info("------> snail-job client remoting server start success, nettype = {}, port = {}",
                    SnailNettyHttpServer.class.getName(), snailJobProperties.getPort());

            started = true;
            future.channel().closeFuture().sync();

        } catch (InterruptedException e) {
            SnailJobLog.LOCAL.info("--------> snail-job client remoting server stop.");
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            SnailJobLog.LOCAL.error("--------> snail-job client remoting server error.", e);
            started = false;
            // Snail Netty Server 未启动
            SnailSpringContext.getContext().publishEvent(new SnailServerStartFailedEvent());
            throw new SnailJobClientException("snail-job client server start error");
        } finally {
            // 当服务器正常关闭时，关闭EventLoopGroups以释放资源。
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }

    @Override
    public void start() {
        if (RpcTypeEnum.NETTY != snailJobProperties.getRpcType()) {
            return;
        }

        thread = new Thread(this);
        thread.setDaemon(true);
        thread.start();
    }

    @Override
    public void close() {
        if (thread != null && thread.isAlive()) {
            thread.interrupt();
        }
    }
}
