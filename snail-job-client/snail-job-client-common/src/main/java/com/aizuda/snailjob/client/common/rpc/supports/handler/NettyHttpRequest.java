package com.aizuda.snailjob.client.common.rpc.supports.handler;

import com.aizuda.snailjob.client.common.rpc.supports.http.HttpRequest;
import com.aizuda.snailjob.client.common.rpc.supports.http.HttpResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import lombok.Builder;
import lombok.Data;

/**
 * netty客户端请求模型
 *
 * @author: opensnail
 * @date : 2023-07-24 09:32
 * @since : 3.3.0
 */
@Data
@Builder
public class NettyHttpRequest {

    private final ChannelHandlerContext channelHandlerContext;
    private final String content;
    private final boolean keepAlive;
    private final HttpMethod method;
    private final String uri;
    private final HttpHeaders headers;
    private final HttpResponse httpResponse;
    private final HttpRequest httpRequest;

}
