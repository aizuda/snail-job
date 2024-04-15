package com.aizuda.snail.job.server.common.dto;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpHeaders;
import lombok.Builder;
import lombok.Data;
import io.netty.handler.codec.http.HttpMethod;

/**
 * netty客户端请求模型
 *
 * @author: opensnail
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
