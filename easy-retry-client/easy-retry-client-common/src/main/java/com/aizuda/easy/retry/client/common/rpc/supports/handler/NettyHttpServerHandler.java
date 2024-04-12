package com.aizuda.easy.retry.client.common.rpc.supports.handler;

import com.aizuda.easy.retry.client.common.rpc.supports.http.HttpRequest;
import com.aizuda.easy.retry.client.common.rpc.supports.http.HttpResponse;
import com.aizuda.easy.retry.common.core.model.EasyRetryRequest;
import com.aizuda.easy.retry.common.core.model.NettyResult;
import com.aizuda.easy.retry.common.core.util.JsonUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;

import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author: opensnail
 * @date : 2024-04-11 16:03
 * @since : 3.3.0
 */
public class NettyHttpServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    // TODO 支持可配置
    private static final ThreadPoolExecutor DISPATCHER_THREAD_POOL = new ThreadPoolExecutor(
            16, 16, 1L, TimeUnit.SECONDS, new LinkedBlockingQueue<>(),
            new CustomizableThreadFactory("snail-netty-server-"));
    private final SnailDispatcherRequestHandler dispatcher;

    public NettyHttpServerHandler(SnailDispatcherRequestHandler snailDispatcherRequestHandler) {
        this.dispatcher = snailDispatcherRequestHandler;
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
                .httpResponse(new HttpResponse())
                .httpRequest(new HttpRequest(headers, uri))
                .build();

        // 执行任务
        DISPATCHER_THREAD_POOL.execute(() -> {
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

    private void writeResponse(ChannelHandlerContext ctx, boolean keepAlive, final HttpResponse httpResponse, String responseJson) {
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
