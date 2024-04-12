package com.aizuda.easy.retry.client.common.netty.server;

import cn.hutool.core.net.url.UrlBuilder;
import cn.hutool.core.util.ServiceLoaderUtil;
import com.aizuda.easy.retry.client.common.config.EasyRetryProperties;
import com.aizuda.easy.retry.client.common.exception.EasyRetryClientException;
import com.aizuda.easy.retry.client.common.intercepter.HandlerInterceptor;
import com.aizuda.easy.retry.client.common.netty.RequestMethod;
import com.aizuda.easy.retry.common.core.constant.SystemConstants;
import com.aizuda.easy.retry.common.core.context.SpringContext;
import com.aizuda.easy.retry.common.core.model.EasyRetryRequest;
import com.aizuda.easy.retry.common.core.model.NettyResult;
import com.aizuda.easy.retry.common.core.model.Result;
import com.aizuda.easy.retry.common.core.util.JsonUtil;
import com.aizuda.easy.retry.common.log.EasyRetryLog;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author: opensnail
 * @date : 2024-04-11 16:03
 * @since : 3.3.0
 */
public class NettyHttpServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    private static final ThreadPoolExecutor DISPATCHER_THREAD_POOL = new ThreadPoolExecutor(
        16, 16, 1L, TimeUnit.SECONDS, new LinkedBlockingQueue<>(),
        new CustomizableThreadFactory("snail-netty-server-"));

    public NettyHttpServerHandler() {
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, FullHttpRequest fullHttpRequest) {

        String content = fullHttpRequest.content().toString(CharsetUtil.UTF_8);
        String uri = fullHttpRequest.uri();
        String name = fullHttpRequest.method().name();
        HttpHeaders headers = fullHttpRequest.headers();

        NettyHttpRequest nettyHttpRequest = NettyHttpRequest.builder()
            .keepAlive(HttpUtil.isKeepAlive(fullHttpRequest))
            .uri(fullHttpRequest.uri())
            .channelHandlerContext(channelHandlerContext)
            .method(fullHttpRequest.method())
            .headers(fullHttpRequest.headers())
            .content(fullHttpRequest.content().toString(CharsetUtil.UTF_8))
            .build();

        // 执行任务
        DISPATCHER_THREAD_POOL.execute(() -> {

            List<HandlerInterceptor> handlerInterceptors = handlerInterceptors();
            EasyRetryRequest retryRequest = JsonUtil.parseObject(content, EasyRetryRequest.class);
            HttpRequest httpRequest = new HttpRequest();
            HttpResponse httpResponse = new HttpResponse();
            EndPointInfo endPointInfo = null;
            Result resultObj = null;
            Exception e;
            try {
                EasyRetryProperties properties = SpringContext.getBean(EasyRetryProperties.class);
                String easyRetryAuth = headers.getAsString(SystemConstants.EASY_RETRY_AUTH_TOKEN);
                String configToken = Optional.ofNullable(properties.getToken()).orElse(SystemConstants.DEFAULT_TOKEN);
                if (!configToken.equals(easyRetryAuth)) {
                    throw new EasyRetryClientException("认证失败.【请检查配置的Token是否正确】");
                }

                UrlBuilder builder = UrlBuilder.ofHttp(uri);
                RequestMethod requestMethod = RequestMethod.valueOf(name);

                endPointInfo = EndPointInfoCache.get(builder.getPathStr(), requestMethod);
                Class<?>[] paramTypes = endPointInfo.getMethod().getParameterTypes();
                Object[] args = retryRequest.getArgs();

                Object[] deSerialize = (Object[]) deSerialize(JsonUtil.toJsonString(args), endPointInfo.getMethod());

                for (final HandlerInterceptor handlerInterceptor : handlerInterceptors) {
                    handlerInterceptor.preHandle(httpRequest, httpResponse, endPointInfo);
                }

                if (paramTypes.length > 0) {
                    resultObj = (Result) ReflectionUtils.invokeMethod(endPointInfo.getMethod(),
                        endPointInfo.getExecutor(), deSerialize);
                } else {
                    resultObj = (Result) ReflectionUtils.invokeMethod(endPointInfo.getMethod(),
                        endPointInfo.getExecutor());
                }

                for (final HandlerInterceptor handlerInterceptor : handlerInterceptors) {
                    handlerInterceptor.postHandle(httpRequest, httpResponse, endPointInfo);
                }
            } catch (Exception ex) {
                EasyRetryLog.LOCAL.error("http request error. [{}]", content, ex);
                resultObj = new NettyResult(0, ex.getMessage(), null, retryRequest.getReqId());
                e = ex;
            } finally {
                NettyResult nettyResult = new NettyResult();
                nettyResult.setRequestId(retryRequest.getReqId());
                if (Objects.nonNull(resultObj)) {
                    nettyResult.setData(resultObj.getData());
                    nettyResult.setMessage(resultObj.getMessage());
                    nettyResult.setStatus(resultObj.getStatus());
                }

                for (final HandlerInterceptor handlerInterceptor : handlerInterceptors) {
                    handlerInterceptor.afterCompletion(httpRequest, httpResponse, endPointInfo, e);
                }

                writeResponse(channelHandlerContext, HttpUtil.isKeepAlive(fullHttpRequest), httpResponse,
                    JsonUtil.toJsonString(nettyResult));

            }
        });
    }

    private void writeResponse(ChannelHandlerContext ctx, boolean keepAlive, final HttpResponse httpResponse, String responseJson) {
        // write response
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK,
            Unpooled.copiedBuffer(responseJson, CharsetUtil.UTF_8));
        response.headers().set(HttpHeaderNames.CONTENT_TYPE,
            HttpHeaderValues.APPLICATION_JSON);
        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
        if (keepAlive) {
            response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
        }
        Map<String, Object> headers = httpResponse.getHeaders();
        headers.forEach((key, value) -> response.headers().set(key, value));
        ctx.writeAndFlush(response);
    }

    public Object deSerialize(String infoStr, Method method) throws JsonProcessingException {

        Type[] paramTypes = method.getGenericParameterTypes();

        Object[] params = new Object[paramTypes.length];

        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = JsonUtil.toJson(infoStr);
        if (Objects.isNull(jsonNode)) {
            EasyRetryLog.LOCAL.warn("jsonNode is null. infoStr:[{}]", infoStr);
            return params;
        }

        for (int i = 0; i < paramTypes.length; i++) {
            JsonNode node = jsonNode.get(i);
            if (Objects.nonNull(node)) {
                params[i] = mapper.readValue(node.toString(), mapper.constructType(paramTypes[i]));
            }
        }

        return params;
    }

    private static List<HandlerInterceptor> handlerInterceptors() {
        List<HandlerInterceptor> handlerInterceptors = ServiceLoaderUtil.loadList(HandlerInterceptor.class);
        if (CollectionUtils.isEmpty(handlerInterceptors)) {
            return Collections.emptyList();
        }

        return handlerInterceptors.stream().sorted(Comparator.comparingInt(HandlerInterceptor::order)).collect(
            Collectors.toList());
    }

}
