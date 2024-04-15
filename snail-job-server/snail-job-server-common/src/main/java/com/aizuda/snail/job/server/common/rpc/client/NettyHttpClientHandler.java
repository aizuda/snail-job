package com.aizuda.snail.job.server.common.rpc.client;

import com.aizuda.snail.job.common.core.model.NettyResult;
import com.aizuda.snail.job.common.core.util.JsonUtil;
import com.aizuda.snail.job.common.log.EasyRetryLog;
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
        NettyChannel.removeChannel(ctx.channel());
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
        EasyRetryLog.LOCAL.error("snail-job netty-http client exception", cause);
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
        EasyRetryLog.LOCAL.debug("userEventTriggered");
    }
}
