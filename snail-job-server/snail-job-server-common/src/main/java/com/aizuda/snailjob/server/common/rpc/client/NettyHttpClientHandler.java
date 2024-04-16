package com.aizuda.snailjob.server.common.rpc.client;

import com.aizuda.snailjob.common.core.model.NettyResult;
import com.aizuda.snailjob.common.core.util.JsonUtil;
import com.aizuda.snailjob.common.log.SnailJobLog;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * netty 客户端处理器
 *
 * @author: opensnail
 * @date : 2022-03-07 18:30
 * @since 1.0.0
 */
@Slf4j
public class NettyHttpClientHandler extends SimpleChannelInboundHandler<FullHttpResponse> {

    public NettyHttpClientHandler() {
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpResponse msg) throws Exception {

        FullHttpResponse response = msg;
        String content = response.content().toString(CharsetUtil.UTF_8);
        HttpHeaders headers = response.headers();

        SnailJobLog.LOCAL.info("Receive server data content:[{}], headers:[{}]", content, headers);
        NettyResult nettyResult = JsonUtil.parseObject(content, NettyResult.class);
        RpcContext.invoke(nettyResult.getRequestId(), nettyResult);

    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        super.channelRegistered(ctx);
        SnailJobLog.LOCAL.debug("channelRegistered");
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        SnailJobLog.LOCAL.debug("channelUnregistered");
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
        NettyChannel.removeChannel(ctx.channel());
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
        NettyChannel.removeChannel(ctx.channel());

    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        super.handlerRemoved(ctx);
        NettyChannel.removeChannel(ctx.channel());
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        SnailJobLog.LOCAL.debug("userEventTriggered");
    }
}
