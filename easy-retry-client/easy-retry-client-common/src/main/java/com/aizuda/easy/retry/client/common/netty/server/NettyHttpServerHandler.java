package com.aizuda.easy.retry.client.common.netty.server;

import cn.hutool.core.net.url.UrlBuilder;
import com.aizuda.easy.retry.client.common.netty.RequestMethod;
import com.aizuda.easy.retry.common.core.model.EasyRetryRequest;
import com.aizuda.easy.retry.common.core.model.Result;
import com.aizuda.easy.retry.common.core.util.JsonUtil;
import com.aizuda.easy.retry.common.log.EasyRetryLog;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import org.springframework.util.ReflectionUtils;

/**
 * @author: opensnail
 * @date : 2024-04-11 16:03
 * @since : 3.3.0
 */
public class NettyHttpServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    public NettyHttpServerHandler() {
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, FullHttpRequest fullHttpRequest)
            throws Exception {

        UrlBuilder builder = UrlBuilder.ofHttp(fullHttpRequest.uri());
        RequestMethod requestMethod = RequestMethod.valueOf(fullHttpRequest.method().name());

        EndPointInfo endPointInfo = EndPointInfoCache.get(builder.getPathStr(), requestMethod);

        Class<?>[] paramTypes = endPointInfo.getMethod().getParameterTypes();
        String content = fullHttpRequest.content().toString(CharsetUtil.UTF_8);
        EasyRetryRequest retryRequest = JsonUtil.parseObject(content, EasyRetryRequest.class);
        Object[] args = retryRequest.getArgs();

        Object resultObj = null;
        try {
            if (paramTypes.length > 0) {
                resultObj = ReflectionUtils.invokeMethod(endPointInfo.getMethod(), endPointInfo.getExecutor(), args);
            } else {
                resultObj = ReflectionUtils.invokeMethod(endPointInfo.getMethod(), endPointInfo.getExecutor());
            }
        } catch (Exception e) {
            EasyRetryLog.LOCAL.error("http request error. [{}]", content, e);
            resultObj = new Result<>(0, e.getMessage());
            throw e;
        } finally {
            writeResponse(channelHandlerContext, HttpUtil.isKeepAlive(fullHttpRequest), JsonUtil.toJsonString(resultObj));
        }

    }

    private void writeResponse(ChannelHandlerContext ctx, boolean keepAlive, String responseJson) {
        // write response
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK,
                Unpooled.copiedBuffer(responseJson, CharsetUtil.UTF_8));
        response.headers().set(HttpHeaderNames.CONTENT_TYPE,
                HttpHeaderValues.APPLICATION_JSON);
        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
        if (keepAlive) {
            response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
        }
        ctx.writeAndFlush(response);
    }


}
