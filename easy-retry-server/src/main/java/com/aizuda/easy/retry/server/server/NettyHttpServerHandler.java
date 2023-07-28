package com.aizuda.easy.retry.server.server;

import akka.actor.ActorRef;
import com.aizuda.easy.retry.server.akka.ActorGenerator;
import com.aizuda.easy.retry.server.dto.NettyHttpRequest;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author: www.byteblogs.com
 * @date : 2022-03-07 16:03
 */
@Slf4j
public class NettyHttpServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    public NettyHttpServerHandler() {
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, FullHttpRequest fullHttpRequest)
        throws Exception {

        NettyHttpRequest nettyHttpRequest = NettyHttpRequest.builder()
            .keepAlive(HttpUtil.isKeepAlive(fullHttpRequest))
            .uri(fullHttpRequest.uri())
            .channelHandlerContext(channelHandlerContext)
            .method(fullHttpRequest.method())
            .headers(fullHttpRequest.headers())
            .content(fullHttpRequest.content().toString(CharsetUtil.UTF_8))
            .build();

        ActorRef actorRef = ActorGenerator.requestHandlerActor();
        actorRef.tell(nettyHttpRequest, actorRef);
    }



}
