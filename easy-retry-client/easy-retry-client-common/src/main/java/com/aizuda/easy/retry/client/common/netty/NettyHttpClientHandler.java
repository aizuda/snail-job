package com.aizuda.easy.retry.client.common.netty;

import com.aizuda.easy.retry.client.common.event.ChannelReconnectEvent;
import com.aizuda.easy.retry.client.common.proxy.RequestBuilder;
import com.aizuda.easy.retry.client.common.NettyClient;
import com.aizuda.easy.retry.common.core.constant.SystemConstants.BEAT;
import com.aizuda.easy.retry.common.core.context.SpringContext;
import com.aizuda.easy.retry.common.log.EasyRetryLog;
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
 * netty 客户端处理器
 *
 * @author: www.byteblogs.com
 * @date : 2022-03-07 18:30
 * @since 1.0.0
 */
@Slf4j
public class NettyHttpClientHandler extends SimpleChannelInboundHandler<FullHttpResponse> {

    private NettyClient client;
    private NettyHttpConnectClient nettyHttpConnectClient;

    public NettyHttpClientHandler(NettyHttpConnectClient nettyHttpConnectClient) {

        client = RequestBuilder.<NettyClient, NettyResult>newBuilder()
            .client(NettyClient.class)
            .callback(nettyResult ->EasyRetryLog.LOCAL.info("heartbeat check requestId:[{}]", nettyResult.getRequestId()))
            .build();

        this.nettyHttpConnectClient = nettyHttpConnectClient;

    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpResponse msg) throws Exception {

        FullHttpResponse response = msg;
        String content = response.content().toString(CharsetUtil.UTF_8);
        HttpHeaders headers = response.headers();

       EasyRetryLog.LOCAL.info("Receive server data content:[{}], headers:[{}]", content, headers);
        NettyResult nettyResult = JsonUtil.parseObject(content, NettyResult.class);
        RpcContext.invoke(nettyResult.getRequestId(), nettyResult);

    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        super.channelRegistered(ctx);
        EasyRetryLog.LOCAL.debug("channelRegistered");
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        EasyRetryLog.LOCAL.debug("channelUnregistered");
        ctx.channel().eventLoop().schedule(() -> {
            try {
                // 抛出重连事件
                SpringContext.getContext().publishEvent(new ChannelReconnectEvent());
                nettyHttpConnectClient.reconnect();
            } catch (Exception e) {
                EasyRetryLog.LOCAL.error("reconnect error ", e);
            }

        }, 10, TimeUnit.SECONDS);


    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        EasyRetryLog.LOCAL.debug("channelActive");
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        EasyRetryLog.LOCAL.debug("channelInactive");
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        super.channelReadComplete(ctx);
        EasyRetryLog.LOCAL.debug("channelReadComplete");
    }

    @Override
    public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
        super.channelWritabilityChanged(ctx);
        EasyRetryLog.LOCAL.debug("channelWritabilityChanged");
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        EasyRetryLog.LOCAL.error("easy-retry netty-http client exception", cause);
        super.exceptionCaught(ctx, cause);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        EasyRetryLog.LOCAL.debug("userEventTriggered");
        if (evt instanceof IdleStateEvent) {
            client.beat(BEAT.PING);
        } else {
            super.userEventTriggered(ctx, evt);
        }

    }
}
