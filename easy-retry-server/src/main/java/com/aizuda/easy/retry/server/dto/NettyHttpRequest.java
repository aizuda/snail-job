package com.aizuda.easy.retry.server.dto;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import lombok.Builder;
import lombok.Data;

/**
 * netty客户端请求模型
 *
 * @author: www.byteblogs.com
 * @date : 2023-07-24 09:32
 */
@Data
@Builder
public class NettyHttpRequest {

    private ChannelHandlerContext channelHandlerContext;

    private String content;

    private boolean keepAlive;

    private HttpMethod method;

    private String uri;

    private HttpHeaders headers;

}
