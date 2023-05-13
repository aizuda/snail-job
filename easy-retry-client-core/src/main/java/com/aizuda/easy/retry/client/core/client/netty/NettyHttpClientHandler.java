package com.aizuda.easy.retry.client.core.client.netty;

import com.aizuda.easy.retry.client.core.client.NettyClient;
import com.aizuda.easy.retry.client.core.client.proxy.RequestBuilder;
import com.aizuda.easy.retry.client.core.config.EasyRetryProperties;
import com.aizuda.easy.retry.common.core.context.SpringContext;
import com.aizuda.easy.retry.common.core.log.LogUtils;
import com.aizuda.easy.retry.common.core.model.NettyResult;
import com.aizuda.easy.retry.common.core.util.JsonUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

/**
 * @author: www.byteblogs.com
 * @date : 2022-03-07 18:30
 */
@Slf4j
public class NettyHttpClientHandler extends SimpleChannelInboundHandler<FullHttpResponse> {

    private NettyClient client;
    private NettyHttpConnectClient nettyHttpConnectClient;

    public NettyHttpClientHandler(NettyHttpConnectClient nettyHttpConnectClient) {

        client = RequestBuilder.<NettyClient, NettyResult>newBuilder()
            .client(NettyClient.class)
            .callback(nettyResult -> LogUtils.info(log,"heartbeat check requestId:[{}]", nettyResult.getRequestId()))
            .build();

        this.nettyHttpConnectClient = nettyHttpConnectClient;

    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpResponse msg) throws Exception {

        FullHttpResponse response = msg;
        String content = response.content().toString(CharsetUtil.UTF_8);
        HttpHeaders headers = response.headers();

        LogUtils.info(log, "Receive server data content:[{}], headers:[{}]", content, headers);
        NettyResult nettyResult = JsonUtil.parseObject(content, NettyResult.class);
        RpcContext.invoke(nettyResult.getRequestId(), nettyResult);

    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        super.channelRegistered(ctx);
        LogUtils.debug(log, "channelRegistered");
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        LogUtils.debug(log, "channelUnregistered");
        ctx.channel().eventLoop().schedule(() -> {
            try {
                nettyHttpConnectClient.reconnect();
            } catch (Exception e) {
                LogUtils.error(log, "reconnect error ", e);
            }

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
        LogUtils.error(log,"easy-retry netty-http client exception", cause);
        super.exceptionCaught(ctx, cause);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        LogUtils.debug(log,"userEventTriggered");
        if (evt instanceof IdleStateEvent) {
            client.beat("PING");
        } else {
            super.userEventTriggered(ctx, evt);
        }

    }
}
