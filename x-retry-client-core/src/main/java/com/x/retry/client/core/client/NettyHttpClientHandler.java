package com.x.retry.client.core.client;

import com.x.retry.client.core.client.request.BeatHttpRequestHandler;
import com.x.retry.client.core.client.request.RequestParam;
import com.x.retry.client.core.client.response.XRetryResponse;
import com.x.retry.client.core.config.XRetryProperties;
import com.x.retry.common.core.context.SpringContext;
import com.x.retry.common.core.log.LogUtils;
import com.x.retry.common.core.model.NettyResult;
import com.x.retry.common.core.model.XRetryRequest;
import com.x.retry.common.core.util.HostUtils;
import com.x.retry.common.core.util.JsonUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @author: www.byteblogs.com
 * @date : 2022-03-07 18:30
 */
@Slf4j
public class NettyHttpClientHandler extends SimpleChannelInboundHandler<FullHttpResponse> {

    private static final String HOST_ID = UUID.randomUUID().toString().concat("_").concat(HostUtils.getIp());

    private NettyHttpConnectClient nettyHttpConnectClient;

    public NettyHttpClientHandler(NettyHttpConnectClient nettyHttpConnectClient) {
        this.nettyHttpConnectClient = nettyHttpConnectClient;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpResponse msg) throws Exception {

        FullHttpResponse response = (FullHttpResponse) msg;
        String content = response.content().toString(CharsetUtil.UTF_8);
        HttpHeaders headers = response.headers();

        LogUtils.info(log, "接收服务端返回数据content:[{}],headers:[{}]", content, headers);
        NettyResult nettyResult = JsonUtil.parseObject(content, NettyResult.class);
        XRetryResponse.invoke(nettyResult.getRequestId(), nettyResult);

    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        super.channelRegistered(ctx);
        LogUtils.debug(log, "channelRegistered");
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        super.channelUnregistered(ctx);
        LogUtils.debug(log, "channelUnregistered");
        ctx.channel().eventLoop().schedule(() -> {
            XRetryProperties xRetryProperties = SpringContext.getBeanByType(XRetryProperties.class);
            XRetryProperties.ServerConfig server = xRetryProperties.getServer();
            LogUtils.info(log, "Reconnecting to:" + server.getHost() + ":" + server.getPort());
            NettyHttpConnectClient.connect();
        }, 10, TimeUnit.SECONDS);


    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        LogUtils.debug(log, "channelActive");
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        LogUtils.debug(log,"channelInactive");
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        super.channelReadComplete(ctx);
        LogUtils.debug(log,"channelReadComplete");
    }

    @Override
    public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
        super.channelWritabilityChanged(ctx);
        LogUtils.debug(log,"channelWritabilityChanged");
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        LogUtils.error(log,"x-retry netty_http client exception", cause);
        super.exceptionCaught(ctx, cause);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        LogUtils.debug(log,"userEventTriggered");
        if (evt instanceof IdleStateEvent) {

            XRetryRequest xRetryRequest = new XRetryRequest("PING");

            BeatHttpRequestHandler requestHandler = SpringContext.getBeanByType(BeatHttpRequestHandler.class);
            XRetryResponse.cache(xRetryRequest, requestHandler.callable());

            NettyHttpConnectClient.send(requestHandler.method(), requestHandler.getHttpUrl(new RequestParam()), requestHandler.body(xRetryRequest));   // beat N, close if fail(may throw error)
        } else {
            super.userEventTriggered(ctx, evt);
        }

    }
}
