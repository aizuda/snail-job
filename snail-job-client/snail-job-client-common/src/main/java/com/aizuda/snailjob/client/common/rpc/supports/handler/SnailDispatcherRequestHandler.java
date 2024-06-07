package com.aizuda.snailjob.client.common.rpc.supports.handler;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.net.url.UrlBuilder;
import cn.hutool.core.util.ServiceLoaderUtil;
import com.aizuda.snailjob.client.common.HandlerInterceptor;
import com.aizuda.snailjob.client.common.cache.EndPointInfoCache;
import com.aizuda.snailjob.client.common.config.SnailJobProperties;
import com.aizuda.snailjob.client.common.exception.SnailJobClientException;
import com.aizuda.snailjob.client.common.rpc.client.RequestMethod;
import com.aizuda.snailjob.client.common.rpc.supports.http.HttpRequest;
import com.aizuda.snailjob.client.common.rpc.supports.http.HttpResponse;
import com.aizuda.snailjob.client.common.rpc.supports.scan.EndPointInfo;
import com.aizuda.snailjob.common.core.constant.SystemConstants;
import com.aizuda.snailjob.common.core.enums.StatusEnum;
import com.aizuda.snailjob.common.core.model.NettyResult;
import com.aizuda.snailjob.common.core.model.Result;
import com.aizuda.snailjob.common.core.model.SnailJobRequest;
import com.aizuda.snailjob.common.core.util.JsonUtil;
import com.aizuda.snailjob.common.log.SnailJobLog;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author: opensnail
 * @date : 2024-04-12
 * @since : 3.3.0
 */
@Component
@RequiredArgsConstructor
public class SnailDispatcherRequestHandler {
    private final SnailJobProperties snailJobProperties;

    public NettyResult dispatch(NettyHttpRequest request) {

        NettyResult nettyResult = new NettyResult();

        List<HandlerInterceptor> handlerInterceptors = handlerInterceptors();
        SnailJobRequest retryRequest = JsonUtil.parseObject(request.getContent(), SnailJobRequest.class);
        HttpRequest httpRequest = request.getHttpRequest();
        HttpResponse httpResponse = request.getHttpResponse();
        EndPointInfo endPointInfo = null;
        Result resultObj = null;
        Exception e = null;
        try {
            String snailJobAuth = request.getHeaders().getAsString(SystemConstants.SNAIL_JOB_AUTH_TOKEN);
            String configToken = Optional.ofNullable(snailJobProperties.getToken()).orElse(SystemConstants.DEFAULT_TOKEN);
            if (!configToken.equals(snailJobAuth)) {
                throw new SnailJobClientException("认证失败.【请检查配置的Token是否正确】");
            }

            UrlBuilder builder = UrlBuilder.ofHttp(request.getUri());
            RequestMethod requestMethod = RequestMethod.valueOf(request.getMethod().name());

            endPointInfo = EndPointInfoCache.get(builder.getPathStr(), requestMethod);
            if (Objects.isNull(endPointInfo)) {
                throw new SnailJobClientException("无法找到对应的处理请检查对应的包是否正确引入. " +
                        "path:[{}] requestMethod:[{}]", builder.getPathStr(), requestMethod);
            }

            Class<?>[] paramTypes = endPointInfo.getMethod().getParameterTypes();
            Object[] args = retryRequest.getArgs();

            Object[] deSerialize = (Object[]) deSerialize(JsonUtil.toJsonString(args), endPointInfo.getMethod(),
                    httpRequest, httpResponse);

            for (final HandlerInterceptor handlerInterceptor : handlerInterceptors) {
                if (!handlerInterceptor.preHandle(httpRequest, httpResponse, endPointInfo)) {
                    return nettyResult;
                }
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
            SnailJobLog.LOCAL.error("http request error. [{}]", request.getContent(), ex);
            nettyResult.setMessage(ex.getMessage()).setStatus(StatusEnum.NO.getStatus());
            e = ex;
        } finally {
            nettyResult.setRequestId(retryRequest.getReqId());
            if (Objects.nonNull(resultObj)) {
                nettyResult.setData(resultObj.getData())
                        .setMessage(resultObj.getMessage())
                        .setStatus(resultObj.getStatus());
            }

            for (final HandlerInterceptor handlerInterceptor : handlerInterceptors) {
                handlerInterceptor.afterCompletion(httpRequest, httpResponse, endPointInfo, e);
            }
        }

        return nettyResult;
    }

    private static List<HandlerInterceptor> handlerInterceptors() {
        List<HandlerInterceptor> handlerInterceptors = ServiceLoaderUtil.loadList(HandlerInterceptor.class);
        if (CollUtil.isEmpty(handlerInterceptors)) {
            return Collections.emptyList();
        }

        return handlerInterceptors.stream().sorted(Comparator.comparingInt(HandlerInterceptor::order)).collect(
                Collectors.toList());
    }

    public Object deSerialize(String infoStr, Method method,
                              HttpRequest httpRequest, HttpResponse httpResponse) throws JsonProcessingException {

        Type[] paramTypes = method.getGenericParameterTypes();
        Parameter[] parameters = method.getParameters();
        Object[] params = new Object[paramTypes.length];

        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = JsonUtil.toJson(infoStr);
        if (Objects.isNull(jsonNode)) {
            SnailJobLog.LOCAL.warn("jsonNode is null. infoStr:[{}]", infoStr);
            return params;
        }

        for (int i = 0; i < paramTypes.length; i++) {
            JsonNode node = jsonNode.get(i);
            if (Objects.nonNull(node)) {
                params[i] = mapper.readValue(node.toString(), mapper.constructType(paramTypes[i]));
                continue;
            }

            Parameter parameter = parameters[i];
            if (parameter.getType().isAssignableFrom(HttpRequest.class)) {
                params[i] = httpRequest;
                continue;
            }

            if (parameter.getType().isAssignableFrom(HttpResponse.class)) {
                params[i] = httpResponse;
            }
        }

        return params;
    }

}
