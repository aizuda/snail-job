package com.aizuda.snailjob.server.common.rpc.server;

import akka.actor.AbstractActor;
import cn.hutool.core.net.url.UrlBuilder;
import cn.hutool.core.util.StrUtil;
import com.aizuda.snailjob.common.core.context.SpringContext;
import com.aizuda.snailjob.common.core.enums.HeadersEnum;
import com.aizuda.snailjob.common.core.enums.StatusEnum;
import com.aizuda.snailjob.common.core.model.NettyResult;
import com.aizuda.snailjob.common.core.model.Result;
import com.aizuda.snailjob.common.core.model.SnailJobRequest;
import com.aizuda.snailjob.common.core.util.JsonUtil;
import com.aizuda.snailjob.common.log.SnailJobLog;
import com.aizuda.snailjob.server.common.HttpRequestHandler;
import com.aizuda.snailjob.server.common.Register;
import com.aizuda.snailjob.server.common.akka.ActorGenerator;
import com.aizuda.snailjob.server.common.cache.CacheToken;
import com.aizuda.snailjob.server.common.dto.NettyHttpRequest;
import com.aizuda.snailjob.server.common.exception.SnailJobServerException;
import com.aizuda.snailjob.server.common.register.ClientRegister;
import com.aizuda.snailjob.server.common.register.RegisterContext;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Collection;

/**
 * 处理netty客户端请求
 *
 * @author: opensnail
 * @date : 2023-07-24 09:20
 * @since 2.1.0
 */
@Component(ActorGenerator.REQUEST_HANDLER_ACTOR)
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Slf4j
public class RequestHandlerActor extends AbstractActor {

    @Override
    public Receive createReceive() {
        return receiveBuilder().match(NettyHttpRequest.class, nettyHttpRequest -> {

            final String uri = nettyHttpRequest.getUri();
            if (StrUtil.isBlank(uri)) {
                SnailJobLog.LOCAL.error("uri can not be null");
                return;
            }

            ChannelHandlerContext channelHandlerContext = nettyHttpRequest.getChannelHandlerContext();

            final boolean keepAlive = nettyHttpRequest.isKeepAlive();
            final HttpMethod method = nettyHttpRequest.getMethod();
            final String content = nettyHttpRequest.getContent();
            final HttpHeaders headers = nettyHttpRequest.getHeaders();

            String result = "";
            try {
                result = doProcess(uri, content, method, headers);
            } catch (Exception e) {
                SnailJobLog.LOCAL.error("http request error. [{}]", nettyHttpRequest.getContent(), e);
                SnailJobRequest retryRequest = JsonUtil.parseObject(content, SnailJobRequest.class);
                result = JsonUtil.toJsonString(new NettyResult(StatusEnum.NO.getStatus(), e.getMessage(), null, retryRequest.getReqId()));
            } finally {
                writeResponse(channelHandlerContext, keepAlive, result);
                getContext().stop(getSelf());
            }


        }).build();
    }

    private String doProcess(String uri, String content, HttpMethod method,
                             HttpHeaders headers) {

        Register register = SpringContext.getBean(ClientRegister.BEAN_NAME, Register.class);

        String hostId = headers.get(HeadersEnum.HOST_ID.getKey());
        String hostIp = headers.get(HeadersEnum.HOST_IP.getKey());
        Integer hostPort = headers.getInt(HeadersEnum.HOST_PORT.getKey());
        String groupName = headers.get(HeadersEnum.GROUP_NAME.getKey());
        String namespace = headers.get(HeadersEnum.NAMESPACE.getKey());
        String token = headers.get(HeadersEnum.TOKEN.getKey());

        if (!CacheToken.get(groupName, namespace).equals(token)) {
            SnailJobLog.LOCAL.error("Token authentication failed. [{}]", token);
            return JsonUtil.toJsonString(new Result<>(0, "Token authentication failed"));
        }

        // 注册版本
        RegisterContext registerContext = new RegisterContext();
        registerContext.setGroupName(groupName);
        registerContext.setHostPort(hostPort);
        registerContext.setHostIp(hostIp);
        registerContext.setHostId(hostId);
        registerContext.setUri(uri);
        registerContext.setNamespaceId(namespace);
        boolean result = register.register(registerContext);
        if (!result) {
            SnailJobLog.LOCAL.warn("client register error. groupName:[{}]", groupName);
        }

        UrlBuilder builder = UrlBuilder.ofHttp(uri);
        Collection<HttpRequestHandler> httpRequestHandlers = SpringContext.getContext()
                .getBeansOfType(HttpRequestHandler.class).values();
        for (HttpRequestHandler httpRequestHandler : httpRequestHandlers) {
            if (httpRequestHandler.supports(builder.getPathStr()) && method.name()
                    .equals(httpRequestHandler.method().name())) {
                return httpRequestHandler.doHandler(content, builder, headers);
            }
        }

        throw new SnailJobServerException("No matching handler found. Path:[{}] method:[{}]", builder.getPathStr(), method.name());
    }

    /**
     * write response
     */
    private void writeResponse(ChannelHandlerContext ctx, boolean keepAlive, String responseJson) {
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK,
                Unpooled.copiedBuffer(responseJson, CharsetUtil.UTF_8));
        response.headers().set(HttpHeaderNames.CONTENT_TYPE,
                HttpHeaderValues.APPLICATION_JSON);
        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
        if (keepAlive) {
            response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
        }
        ctx.writeAndFlush(response);
    }

}
