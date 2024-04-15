package com.aizuda.snail.job.client.common.rpc.supports.handler;

import com.aizuda.snail.job.client.common.config.SnailJobProperties;
import com.aizuda.snail.job.client.common.config.SnailJobProperties.DispatcherThreadPool;
import com.aizuda.snail.job.client.common.rpc.supports.http.HttpResponse;
import com.aizuda.snail.job.common.core.model.EasyRetryRequest;
import com.aizuda.snail.job.common.core.model.NettyResult;
import com.aizuda.snail.job.common.core.util.JsonUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;

import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author: opensnail
 * @date : 2024-04-11 16:03
 * @since : 3.3.0
 */
public class NettyHttpServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    private final ThreadPoolExecutor dispatcherThreadPool;
    private final SnailDispatcherRequestHandler dispatcher;

    public NettyHttpServerHandler(SnailDispatcherRequestHandler snailDispatcherRequestHandler,
        SnailJobProperties snailJobProperties) {
        this.dispatcher = snailDispatcherRequestHandler;

        // 获取线程池配置
        DispatcherThreadPool threadPool = snailJobProperties.getDispatcherThreadPool();

        dispatcherThreadPool = new ThreadPoolExecutor(
            threadPool.getCorePoolSize(), threadPool.getMaximumPoolSize(), threadPool.getKeepAliveTime(),
            threadPool.getTimeUnit(), new LinkedBlockingQueue<>(threadPool.getQueueCapacity()),
            new CustomizableThreadFactory("snail-netty-server-"));
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, FullHttpRequest fullHttpRequest) {

        String content = fullHttpRequest.content().toString(CharsetUtil.UTF_8);
        HttpHeaders headers = fullHttpRequest.headers();
        String uri = fullHttpRequest.uri();
        NettyHttpRequest nettyHttpRequest = NettyHttpRequest.builder()
            .keepAlive(HttpUtil.isKeepAlive(fullHttpRequest))
            .uri(uri)
            .channelHandlerContext(channelHandlerContext)
            .method(fullHttpRequest.method())
            .headers(headers)
            .content(content)
            .httpResponse(new com.aizuda.snail.job.client.common.rpc.supports.http.HttpResponse())
            .httpRequest(new com.aizuda.snail.job.client.common.rpc.supports.http.HttpRequest(headers, uri))
            .build();

        // 执行任务
        dispatcherThreadPool.execute(() -> {
            NettyResult nettyResult = null;
            try {
                nettyResult = dispatcher.dispatch(nettyHttpRequest);
            } catch (Exception e) {
                EasyRetryRequest retryRequest = JsonUtil.parseObject(content, EasyRetryRequest.class);
                nettyResult = new NettyResult(0, e.getMessage(), null, retryRequest.getReqId());
            } finally {
                writeResponse(channelHandlerContext,
                    HttpUtil.isKeepAlive(fullHttpRequest),
                    nettyHttpRequest.getHttpResponse(),
                    JsonUtil.toJsonString(nettyResult)
                );
            }
        });
    }

    private void writeResponse(ChannelHandlerContext ctx, boolean keepAlive, final HttpResponse httpResponse,
        String responseJson) {
        // write response
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK,
            Unpooled.copiedBuffer(responseJson, CharsetUtil.UTF_8));
        response.headers().set(HttpHeaderNames.CONTENT_TYPE,
            HttpHeaderValues.APPLICATION_JSON);
        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
        if (keepAlive) {
            response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
        }
        Map<String, Object> headers = httpResponse.getHeaders();
        headers.forEach((key, value) -> response.headers().set(key, value));
        ctx.writeAndFlush(response);
    }

}
