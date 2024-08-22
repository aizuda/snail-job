package com.aizuda.snailjob.server.common.rpc.server;

import akka.actor.AbstractActor;
import cn.hutool.core.net.url.UrlBuilder;
import cn.hutool.core.util.StrUtil;
import com.aizuda.snailjob.common.core.context.SnailSpringContext;
import com.aizuda.snailjob.common.core.enums.HeadersEnum;
import com.aizuda.snailjob.common.core.enums.StatusEnum;
import com.aizuda.snailjob.common.core.grpc.auto.GrpcResult;
import com.aizuda.snailjob.common.core.grpc.auto.GrpcSnailJobRequest;
import com.aizuda.snailjob.common.core.grpc.auto.Metadata;
import com.aizuda.snailjob.common.core.model.NettyResult;
import com.aizuda.snailjob.common.core.model.SnailJobRequest;
import com.aizuda.snailjob.common.core.util.JsonUtil;
import com.aizuda.snailjob.common.log.SnailJobLog;
import com.aizuda.snailjob.server.common.HttpRequestHandler;
import com.aizuda.snailjob.server.common.Register;
import com.aizuda.snailjob.server.common.akka.ActorGenerator;
import com.aizuda.snailjob.server.common.cache.CacheToken;
import com.aizuda.snailjob.server.common.dto.GrpcRequest;
import com.aizuda.snailjob.server.common.exception.SnailJobServerException;
import com.aizuda.snailjob.server.common.register.ClientRegister;
import com.aizuda.snailjob.server.common.register.RegisterContext;
import com.fasterxml.jackson.databind.util.ByteBufferBackedInputStream;
import com.google.protobuf.Any;
import com.google.protobuf.ByteString;
import com.google.protobuf.UnsafeByteOperations;
import io.grpc.stub.StreamObserver;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

import static com.aizuda.snailjob.common.core.alarm.AlarmContext.build;

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
public class GrpcRequestHandlerActor extends AbstractActor {

    @Override
    public Receive createReceive() {
        return receiveBuilder().match(GrpcRequest.class, grpcRequest -> {
            GrpcSnailJobRequest grpcSnailJobRequest = grpcRequest.getSnailJobRequest();
            Metadata metadata = grpcSnailJobRequest.getMetadata();
            final String uri = metadata.getUri();
            if (StrUtil.isBlank(uri)) {
                SnailJobLog.LOCAL.error("uri can not be null");
                return;
            }

            Map<String, String> headersMap = metadata.getHeadersMap();
            String result = "";
            try {
                SnailJobRequest request = new SnailJobRequest();
                Any body = grpcSnailJobRequest.getBody();
                ByteString byteString = body.getValue();
                ByteBuffer byteBuffer = byteString.asReadOnlyByteBuffer();
                Object[] objects = JsonUtil.parseObject(new ByteBufferBackedInputStream(byteBuffer), Object[].class);
                request.setArgs(objects);
                result = doProcess(uri, JsonUtil.toJsonString(request), headersMap);
            } catch (Exception e) {
                SnailJobLog.LOCAL.error("http request error. [{}]",grpcSnailJobRequest, e);
                result = JsonUtil.toJsonString(new NettyResult(StatusEnum.NO.getStatus(), e.getMessage(), null, 0));
            } finally {

                NettyResult nettyResult = JsonUtil.parseObject(result, NettyResult.class);
                StreamObserver<GrpcResult> streamObserver = grpcRequest.getStreamObserver();
                GrpcResult grpcResult = GrpcResult.newBuilder()
                    .setStatus(nettyResult.getStatus())
                    .setMessage(Optional.ofNullable(nettyResult.getMessage()).orElse(StrUtil.EMPTY))
                    .setData(Any.newBuilder()
                        .setValue(UnsafeByteOperations.unsafeWrap(JsonUtil.toJsonString(nettyResult.getData()).getBytes()))
                            .build())
                    .build();

                streamObserver.onNext(grpcResult);
                streamObserver.onCompleted();
                getContext().stop(getSelf());
            }


        }).build();
    }

    private String doProcess(String uri, String content, Map<String, String> headersMap) {

        Register register = SnailSpringContext.getBean(ClientRegister.BEAN_NAME, Register.class);

        String hostId = headersMap.get(HeadersEnum.HOST_ID.getKey());
        String hostIp = headersMap.get(HeadersEnum.HOST_IP.getKey());
        Integer hostPort = Integer.valueOf(headersMap.get(HeadersEnum.HOST_PORT.getKey()));
        String groupName = headersMap.get(HeadersEnum.GROUP_NAME.getKey());
        String namespace = headersMap.get(HeadersEnum.NAMESPACE.getKey());
        String token = headersMap.get(HeadersEnum.TOKEN.getKey());

        if (StrUtil.isBlank(token) || !CacheToken.get(groupName, namespace).equals(token)) {
            throw new SnailJobServerException("Token authentication failed. [namespace:{} groupName:{} token:{}]",
                namespace, groupName, token);
        }

        // 注册版本 此后后续版本将迁移至BeatHttpRequestHandler 只处理beat的心态注册
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

        DefaultHttpHeaders headers = new DefaultHttpHeaders();
        headersMap.forEach(headers::add);

        UrlBuilder builder = UrlBuilder.ofHttp(uri);
        Collection<HttpRequestHandler> httpRequestHandlers = SnailSpringContext.getContext()
            .getBeansOfType(HttpRequestHandler.class).values();
        for (HttpRequestHandler httpRequestHandler : httpRequestHandlers) {
            if (httpRequestHandler.supports(builder.getPathStr())) {
                return httpRequestHandler.doHandler(content, builder, headers);
            }
        }

        throw new SnailJobServerException("No matching handler found. Path:[{}] method:[{}]", builder.getPathStr());
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
