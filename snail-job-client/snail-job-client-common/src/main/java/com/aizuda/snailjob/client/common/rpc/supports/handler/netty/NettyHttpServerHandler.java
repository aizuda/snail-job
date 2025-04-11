package com.aizuda.snailjob.client.common.rpc.supports.handler.netty;

import com.aizuda.snailjob.client.common.config.SnailJobProperties;
import com.aizuda.snailjob.client.common.config.SnailJobProperties.RpcServerProperties;
import com.aizuda.snailjob.client.common.config.SnailJobProperties.ThreadPoolConfig;
import com.aizuda.snailjob.client.common.rpc.supports.handler.SnailDispatcherRequestHandler;
import com.aizuda.snailjob.client.common.rpc.supports.http.HttpResponse;
import com.aizuda.snailjob.common.core.enums.StatusEnum;
import com.aizuda.snailjob.common.core.model.SnailJobRpcResult;
import com.aizuda.snailjob.common.core.model.SnailJobRequest;
import com.aizuda.snailjob.common.core.util.JsonUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
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
        RpcServerProperties rpcServerProperties = snailJobProperties.getServerRpc();
        ThreadPoolConfig threadPool = rpcServerProperties.getDispatcherTp();
        dispatcherThreadPool = new ThreadPoolExecutor(
                threadPool.getCorePoolSize(), threadPool.getMaximumPoolSize(), threadPool.getKeepAliveTime(),
                threadPool.getTimeUnit(), new LinkedBlockingQueue<>(threadPool.getQueueCapacity()),
                new CustomizableThreadFactory("snail-netty-server-"));
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, FullHttpRequest fullHttpRequest) {

        String content = fullHttpRequest.content().toString(CharsetUtil.UTF_8);
        HttpHeaders headers = fullHttpRequest.headers();
        Map<String, String> headerMap = new HashMap<>();

        for (final Entry<String, String> header : headers) {
            headerMap.put(header.getKey(), header.getValue());
        }

        String uri = fullHttpRequest.uri();
        NettyHttpRequest nettyHttpRequest = NettyHttpRequest.builder()
                .keepAlive(HttpUtil.isKeepAlive(fullHttpRequest))
                .uri(uri)
                .channelHandlerContext(channelHandlerContext)
                .method(fullHttpRequest.method())
                .headers(headers)
                .content(content)
                .httpResponse(new com.aizuda.snailjob.client.common.rpc.supports.http.HttpResponse())
                .httpRequest(new com.aizuda.snailjob.client.common.rpc.supports.http.HttpRequest(headerMap, uri))
                .build();

        // 执行任务
        dispatcherThreadPool.execute(() -> {
            SnailJobRpcResult snailJobRpcResult = null;
            try {
                snailJobRpcResult = dispatcher.dispatch(nettyHttpRequest);
            } catch (Exception e) {
                SnailJobRequest retryRequest = JsonUtil.parseObject(content, SnailJobRequest.class);
                snailJobRpcResult = new SnailJobRpcResult(StatusEnum.NO.getStatus(), e.getMessage(), null, retryRequest.getReqId());
            } finally {
                writeResponse(channelHandlerContext,
                        HttpUtil.isKeepAlive(fullHttpRequest),
                        nettyHttpRequest.getHttpResponse(),
                        JsonUtil.toJsonString(snailJobRpcResult)
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
