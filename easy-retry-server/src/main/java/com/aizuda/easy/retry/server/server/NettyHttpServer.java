package com.aizuda.easy.retry.server.server;

import com.aizuda.easy.retry.server.exception.EasyRetryServerException;
import com.aizuda.easy.retry.server.config.SystemProperties;
import com.aizuda.easy.retry.server.support.Lifecycle;
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
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.*;

/**
 * @author: www.byteblogs.com
 * @date : 2022-03-07 15:54
 */
@Component
@Slf4j
public class NettyHttpServer implements Runnable, Lifecycle {

    public static final int CPU_NUM = Runtime.getRuntime().availableProcessors();

    @Autowired
    private SystemProperties systemProperties;

    @Override
    public void run() {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        ThreadPoolExecutor serverHandlerPool = new ThreadPoolExecutor(CPU_NUM + 1, 2 * CPU_NUM, 60L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(2000), r -> new Thread(r, "easy-retry-server-handler-pool-" + r.hashCode()), (r, executor) -> {
            throw new EasyRetryServerException("easy-retry thread pool is EXHAUSTED!");
        });

        try {
            // start server
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel channel) throws Exception {
                            channel.pipeline()
                                    .addLast(new IdleStateHandler(0, 0, 30 * 3, TimeUnit.SECONDS))  // beat 3N, close if idle
                                    .addLast(new HttpServerCodec())
                                    .addLast(new HttpObjectAggregator(5 * 1024 * 1024))  // merge request & reponse to FULL
                                    .addLast(new NettyHttpServerHandler(serverHandlerPool));
                        }
                    })
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

            // bind
            ChannelFuture future = bootstrap.bind(systemProperties.getNettyPort()).sync();

            log.info("------> easy-retry remoting server start success, nettype = {}, port = {}", NettyHttpServer.class.getName(), systemProperties.getNettyPort());

            future.channel().closeFuture().sync();

        } catch (InterruptedException e) {
            log.info("--------> easy-retry remoting server stop.");
        } catch (Exception e) {
            log.error("--------> easy-retry remoting server error.", e);
        } finally {

            // stop
            try {
                serverHandlerPool.shutdown();
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
            try {
                workerGroup.shutdownGracefully();
                bossGroup.shutdownGracefully();
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
    }

    @Override
    public void start() {
        Thread thread = new Thread(this);
        thread.setDaemon(true);
        thread.start();
    }

    @Override
    public void close() {

    }
}
