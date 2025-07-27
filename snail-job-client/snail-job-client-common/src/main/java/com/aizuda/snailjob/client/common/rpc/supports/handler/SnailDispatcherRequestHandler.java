package com.aizuda.snailjob.client.common.rpc.supports.handler;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.net.url.UrlBuilder;
import cn.hutool.core.util.ServiceLoaderUtil;
import com.aizuda.snailjob.client.common.HandlerInterceptor;
import com.aizuda.snailjob.client.common.cache.EndPointInfoCache;
import com.aizuda.snailjob.client.common.config.SnailJobProperties;
import com.aizuda.snailjob.client.common.exception.SnailJobClientException;
import com.aizuda.snailjob.client.common.rpc.client.RequestMethod;
import com.aizuda.snailjob.client.common.rpc.supports.handler.grpc.GrpcRequest;
import com.aizuda.snailjob.client.common.rpc.supports.http.HttpRequest;
import com.aizuda.snailjob.client.common.rpc.supports.http.HttpResponse;
import com.aizuda.snailjob.client.common.rpc.supports.scan.EndPointInfo;
import com.aizuda.snailjob.common.core.constant.SystemConstants;
import com.aizuda.snailjob.common.core.enums.StatusEnum;
import com.aizuda.snailjob.common.core.grpc.auto.SnailJobGrpcRequest;
import com.aizuda.snailjob.common.core.grpc.auto.Metadata;
import com.aizuda.snailjob.common.core.model.SnailJobRpcResult;
import com.aizuda.snailjob.common.core.model.Result;
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
    public SnailJobRpcResult dispatch(GrpcRequest request) {
        SnailJobRpcResult snailJobRpcResult = new SnailJobRpcResult();

        HttpRequest httpRequest = request.getHttpRequest();
        HttpResponse httpResponse = request.getHttpResponse();

        List<HandlerInterceptor> handlerInterceptors = handlerInterceptors();

        SnailJobGrpcRequest snailJobRequest = request.getSnailJobRequest();
        EndPointInfo endPointInfo = null;
        Result resultObj = null;
        Throwable e = null;
        try {
            Metadata metadata = snailJobRequest.getMetadata();
            Map<String, String> headersMap = metadata.getHeadersMap();
            String snailJobAuth = headersMap.get(SystemConstants.SNAIL_JOB_AUTH_TOKEN);
            String configToken = Optional.ofNullable(snailJobProperties.getToken()).orElse(SystemConstants.DEFAULT_TOKEN);
            if (!configToken.equals(snailJobAuth)) {
                throw new SnailJobClientException("Authentication failed. [Please check if the configured Token is correct]");
            }

            UrlBuilder builder = UrlBuilder.ofHttp(httpRequest.getUri());
            endPointInfo = EndPointInfoCache.get(builder.getPathStr(), RequestMethod.POST);
            if (Objects.isNull(endPointInfo)) {
                throw new SnailJobClientException(" Cannot find corresponding processing, please check if the corresponding package is correctly introduced." +
                                                  "path:[{}] requestMethod:[{}]", builder.getPathStr());
            }

            Class<?>[] paramTypes = endPointInfo.getMethod().getParameterTypes();
            SnailJobGrpcRequest snailJobGrpcRequest = request.getSnailJobRequest();
            Object[] args = JsonUtil.parseObject(snailJobGrpcRequest.getBody(), Object[].class);

            Object[] deSerialize = (Object[]) deSerialize(JsonUtil.toJsonString(args), endPointInfo.getMethod(),
                httpRequest, httpResponse);

            for (final HandlerInterceptor handlerInterceptor : handlerInterceptors) {
                if (!handlerInterceptor.preHandle(httpRequest, httpResponse, endPointInfo)) {
                    return snailJobRpcResult;
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
        } catch (Throwable ex) {
            SnailJobLog.LOCAL.error("http request error. [{}]", snailJobRequest, ex);
            snailJobRpcResult.setMessage(ex.getMessage()).setStatus(StatusEnum.NO.getStatus());
            e = ex;
        } finally {
            snailJobRpcResult.setReqId(0);
            if (Objects.nonNull(resultObj)) {
                snailJobRpcResult.setData(resultObj.getData())
                    .setMessage(resultObj.getMessage())
                    .setStatus(resultObj.getStatus());
            }

            for (final HandlerInterceptor handlerInterceptor : handlerInterceptors) {
                handlerInterceptor.afterCompletion(httpRequest, httpResponse, endPointInfo, e);
            }
        }

        return snailJobRpcResult;
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
