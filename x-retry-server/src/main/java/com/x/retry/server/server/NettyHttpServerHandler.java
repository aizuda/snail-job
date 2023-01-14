package com.x.retry.server.server;

import cn.hutool.core.net.url.UrlBuilder;
import com.x.retry.common.core.context.SpringContext;
import com.x.retry.common.core.enums.HeadersEnum;
import com.x.retry.common.core.model.Result;
import com.x.retry.common.core.util.JsonUtil;
import com.x.retry.server.exception.XRetryServerException;
import com.x.retry.server.server.handler.HttpRequestHandler;
import com.x.retry.server.support.handler.ClientRegisterHandler;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import org.apache.commons.lang.StringUtils;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.URI;
import java.util.Collection;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author: www.byteblogs.com
 * @date : 2022-03-07 16:03
 */
public class NettyHttpServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    private ThreadPoolExecutor threadPoolExecutor;
    public NettyHttpServerHandler(final ThreadPoolExecutor serverHandlerPool) {
        this.threadPoolExecutor = serverHandlerPool;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, FullHttpRequest fullHttpRequest) throws Exception {
        final String content = fullHttpRequest.content().toString(CharsetUtil.UTF_8);
        final boolean keepAlive = HttpUtil.isKeepAlive(fullHttpRequest);
        final HttpMethod method = fullHttpRequest.method();
        final  String uri = fullHttpRequest.uri();

        threadPoolExecutor.execute(() -> {

            String result = doProcess(channelHandlerContext, uri, content, method, fullHttpRequest.headers());

            writeResponse(channelHandlerContext, keepAlive, result);
        });
    }

    private String doProcess(ChannelHandlerContext channelHandlerContext, String uri, String content, HttpMethod method, HttpHeaders headers) {

        if (StringUtils.isBlank(uri)) {
            throw new XRetryServerException("uri 不能为空");
        }

        ClientRegisterHandler registerHandler = SpringContext.getBeanByType(ClientRegisterHandler.class);
        registerHandler.registerClient(headers);

        Integer clientVersion = headers.getInt(HeadersEnum.VERSION.getKey());
        String groupName = headers.get(HeadersEnum.GROUP_NAME.getKey());
        String hostIp = headers.get(HeadersEnum.HOST_IP.getKey());
        Integer hostPort = headers.getInt(HeadersEnum.HOST_PORT.getKey());

        registerHandler.syncVersion(clientVersion, groupName, hostIp, hostPort);

        UrlBuilder builder = UrlBuilder.ofHttp(uri);
        Collection<HttpRequestHandler> httpRequestHandlers = SpringContext.applicationContext.getBeansOfType(HttpRequestHandler.class).values();
        for (HttpRequestHandler httpRequestHandler : httpRequestHandlers) {
            if (httpRequestHandler.supports(builder.getPathStr()) && method.name().equals(httpRequestHandler.method().name())) {
              return httpRequestHandler.doHandler(content, builder, headers);
            }
        }

        return JsonUtil.toJsonString(new Result<>());
    }

    /**
     * write response
     */
    private void writeResponse(ChannelHandlerContext ctx, boolean keepAlive, String responseJson) {
        // write response
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, Unpooled.copiedBuffer(responseJson, CharsetUtil.UTF_8));   //  Unpooled.wrappedBuffer(responseJson)
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON);       // HttpHeaderValues.TEXT_PLAIN.toString()
        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
        if (keepAlive) {
            response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
        }
        ctx.writeAndFlush(response);
    }



}
