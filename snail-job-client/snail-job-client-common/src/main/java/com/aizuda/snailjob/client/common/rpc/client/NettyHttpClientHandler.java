package com.aizuda.snailjob.client.common.rpc.client;

import com.aizuda.snailjob.client.common.event.SnailChannelReconnectEvent;
import com.aizuda.snailjob.client.common.handler.ClientRegister;
import com.aizuda.snailjob.common.core.constant.SystemConstants.BEAT;
import com.aizuda.snailjob.common.core.context.SnailSpringContext;
import com.aizuda.snailjob.common.core.enums.StatusEnum;
import com.aizuda.snailjob.common.core.model.NettyResult;
import com.aizuda.snailjob.common.core.rpc.RpcContext;
import com.aizuda.snailjob.common.core.util.JsonUtil;
import com.aizuda.snailjob.common.log.SnailJobLog;
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
 * @author: opensnail
 * @date : 2022-03-07 18:30
 * @since 1.0.0
 */
@Slf4j
public class NettyHttpClientHandler extends SimpleChannelInboundHandler<FullHttpResponse> {

    private final NettyHttpConnectClient nettyHttpConnectClient;

    public NettyHttpClientHandler(NettyHttpConnectClient nettyHttpConnectClient) {
        this.nettyHttpConnectClient = nettyHttpConnectClient;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpResponse msg) throws Exception {

        FullHttpResponse response = msg;
        String content = response.content().toString(CharsetUtil.UTF_8);
        HttpHeaders headers = response.headers();

        SnailJobLog.LOCAL.debug("Receive server data content:[{}], headers:[{}]", content, headers);
        NettyResult nettyResult = JsonUtil.parseObject(content, NettyResult.class);
        RpcContext.invoke(nettyResult.getReqId(), nettyResult, false);

    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        super.channelRegistered(ctx);
        SnailJobLog.LOCAL.debug("channelRegistered");
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        SnailJobLog.LOCAL.debug("channelUnregistered");
        ctx.channel().eventLoop().schedule(() -> {
            try {
                // 抛出重连事件
                SnailSpringContext.getContext().publishEvent(new SnailChannelReconnectEvent());
                nettyHttpConnectClient.reconnect();
            } catch (Exception e) {
                SnailJobLog.LOCAL.error("reconnect error ", e);
            }

        }, 10, TimeUnit.SECONDS);


    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        SnailJobLog.LOCAL.debug("channelActive");
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        SnailJobLog.LOCAL.debug("channelInactive");
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        super.channelReadComplete(ctx);
        SnailJobLog.LOCAL.debug("channelReadComplete");
    }

    @Override
    public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
        super.channelWritabilityChanged(ctx);
        SnailJobLog.LOCAL.debug("channelWritabilityChanged");
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        SnailJobLog.LOCAL.error("snail-job netty-http client exception", cause);
        super.exceptionCaught(ctx, cause);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        SnailJobLog.LOCAL.debug("userEventTriggered");
        if (evt instanceof IdleStateEvent) {
            ClientRegister.CLIENT.beat(BEAT.PING);
        } else {
            super.userEventTriggered(ctx, evt);
        }

    }
}
