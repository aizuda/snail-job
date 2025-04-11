package com.aizuda.snailjob.server.common.rpc.server.grpc;

import  org.apache.pekko.actor.AbstractActor;
import cn.hutool.core.net.url.UrlBuilder;
import cn.hutool.core.util.StrUtil;
import com.aizuda.snailjob.common.core.context.SnailSpringContext;
import com.aizuda.snailjob.common.core.enums.HeadersEnum;
import com.aizuda.snailjob.common.core.enums.StatusEnum;
import com.aizuda.snailjob.common.core.grpc.auto.GrpcResult;
import com.aizuda.snailjob.common.core.grpc.auto.SnailJobGrpcRequest;
import com.aizuda.snailjob.common.core.grpc.auto.Metadata;
import com.aizuda.snailjob.common.core.model.SnailJobRpcResult;
import com.aizuda.snailjob.common.core.model.SnailJobRequest;
import com.aizuda.snailjob.common.core.util.JsonUtil;
import com.aizuda.snailjob.common.log.SnailJobLog;
import com.aizuda.snailjob.server.common.HttpRequestHandler;
import com.aizuda.snailjob.server.common.pekko.ActorGenerator;
import com.aizuda.snailjob.server.common.cache.CacheToken;
import com.aizuda.snailjob.server.common.dto.GrpcRequest;
import com.aizuda.snailjob.server.common.exception.SnailJobServerException;
import io.grpc.stub.StreamObserver;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * 处理netty客户端请求
 *
 * @author: opensnail
 * @date : 2023-07-24 09:20
 * @since 2.1.0
 */
@Component(ActorGenerator.GRPC_REQUEST_HANDLER_ACTOR)
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Slf4j
public class GrpcRequestHandlerActor extends AbstractActor {

    @Override
    public Receive createReceive() {
        return receiveBuilder().match(GrpcRequest.class, grpcRequest -> {
            SnailJobGrpcRequest snailJobGrpcRequest = grpcRequest.getSnailJobRequest();
            Metadata metadata = snailJobGrpcRequest.getMetadata();
            final String uri = metadata.getUri();
            if (StrUtil.isBlank(uri)) {
                SnailJobLog.LOCAL.error("uri can not be null");
                return;
            }

            Map<String, String> headersMap = metadata.getHeadersMap();
            SnailJobRpcResult snailJobRpcResult = null;
            try {
                SnailJobRequest request = new SnailJobRequest();
                String body = snailJobGrpcRequest.getBody();
                Object[] objects = JsonUtil.parseObject(body, Object[].class);
                request.setArgs(objects);
                request.setReqId(snailJobGrpcRequest.getReqId());
                snailJobRpcResult = doProcess(uri, JsonUtil.toJsonString(request), headersMap);
                if (Objects.isNull(snailJobRpcResult)) {
                    snailJobRpcResult = new SnailJobRpcResult(StatusEnum.NO.getStatus(), "服务端异常", null,
                        snailJobGrpcRequest.getReqId());
                }
            } catch (Exception e) {
                SnailJobLog.LOCAL.error("http request error. [{}]", snailJobGrpcRequest, e);
                snailJobRpcResult = new SnailJobRpcResult(StatusEnum.NO.getStatus(), e.getMessage(), null,
                    snailJobGrpcRequest.getReqId());
            } finally {
                StreamObserver<GrpcResult> streamObserver = grpcRequest.getStreamObserver();
                GrpcResult grpcResult = GrpcResult.newBuilder()
                    .setReqId(snailJobRpcResult.getReqId())
                    .setStatus(snailJobRpcResult.getStatus())
                    .setMessage(Optional.ofNullable(snailJobRpcResult.getMessage()).orElse(StrUtil.EMPTY))
                    .setData(JsonUtil.toJsonString(snailJobRpcResult.getData()))
                    .build();
                streamObserver.onNext(grpcResult);
                streamObserver.onCompleted();
                getContext().stop(getSelf());
            }


        }).build();
    }

    private SnailJobRpcResult doProcess(String uri, String content, Map<String, String> headersMap) {
        String groupName = headersMap.get(HeadersEnum.GROUP_NAME.getKey());
        String namespace = headersMap.get(HeadersEnum.NAMESPACE.getKey());
        String token = headersMap.get(HeadersEnum.TOKEN.getKey());

        if (StrUtil.isBlank(token) || !CacheToken.get(groupName, namespace).equals(token)) {
            throw new SnailJobServerException("Token authentication failed. [namespace:{} groupName:{} token:{}]",
                namespace, groupName, token);
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


}
