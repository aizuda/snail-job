package com.aizuda.snailjob.client.common.rpc.client.openapi;

import cn.hutool.core.util.ServiceLoaderUtil;
import cn.hutool.core.util.StrUtil;
import com.aizuda.snailjob.client.common.annotation.Header;
import com.aizuda.snailjob.client.common.annotation.Mapping;
import com.aizuda.snailjob.client.common.annotation.Param;
import com.aizuda.snailjob.client.common.config.SnailJobProperties;
import com.aizuda.snailjob.common.core.constant.SystemConstants;
import com.aizuda.snailjob.common.core.context.SnailSpringContext;
import com.aizuda.snailjob.common.core.enums.ExecutorTypeEnum;
import com.aizuda.snailjob.common.core.enums.HeadersEnum;
import com.aizuda.snailjob.common.core.model.Result;
import com.aizuda.snailjob.common.core.util.JsonUtil;
import com.aizuda.snailjob.common.core.util.SnailJobVersion;
import lombok.AllArgsConstructor;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * 请求服务端
 * </p>
 *
 * @author opensnail
 * @date 2025-07-05
 */
@AllArgsConstructor
public class HttpClientInvokeHandler<R extends Result<Object>> implements InvocationHandler {
    private final long timeout;
    private final TimeUnit unit;

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        SnailHttpClient snailHttpClient = loadSnailJobHttpClient();
        Mapping mapping = method.getAnnotation(Mapping.class);
        Parameter[] parameters = method.getParameters();

        Request request = new Request();
        request.setParams(getParams(args, parameters));
        request.setMethod(mapping.method().name());
        request.setPath(mapping.path());
        request.setHeaders(getHeaderInfo(method, args));
        request.setTimeout(unit.toMillis(timeout));
        request.setBody(JsonUtil.toJsonString(args[0]));
        return snailHttpClient.execute(request);
    }

    private String getParams(Object[] args, Parameter[] parameters) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            if (parameter.isAnnotationPresent(Param.class)) {
                Param param = parameter.getAnnotation(Param.class);
                sb.append(param.value()).append("=").append(args[i]);
            }
        }
        String data = sb.toString();
        if (StrUtil.isNotBlank(data)){
            return "?" + data;
        } else {
            return "";
        }
    }

    private Map<String, String> getHeaderInfo(Method method, Object[] args) {
        Map<String, String> headersMap = new HashMap<>();
        Parameter[] parameters = method.getParameters();
        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            if (parameter.isAnnotationPresent(Header.class)) {
                Header header = parameter.getAnnotation(Header.class);
                Object o = args[i];
                if (Objects.nonNull(o)) {
                    headersMap.put(header.name().getKey(), JsonUtil.toJsonString(o));
                }
            }
        }
        SnailJobProperties snailJobProperties = SnailSpringContext.getBean(SnailJobProperties.class);
        SnailJobProperties.ServerConfig serverConfig = snailJobProperties.getServer();
        headersMap.put(HeadersEnum.GROUP_NAME.getKey(), snailJobProperties.getGroup());
        headersMap.put(HeadersEnum.HOST.getKey(), serverConfig.getHost());
        headersMap.put(HeadersEnum.NAMESPACE.getKey(), Optional.ofNullable(snailJobProperties.getNamespace()).orElse(
                SystemConstants.DEFAULT_NAMESPACE));
        headersMap.put(HeadersEnum.TOKEN.getKey(), Optional.ofNullable(snailJobProperties.getToken()).orElse(
                SystemConstants.DEFAULT_TOKEN));
        headersMap.put(HeadersEnum.SYSTEM_VERSION.getKey(), Optional.ofNullable(SnailJobVersion.getVersion()).orElse(
                SystemConstants.DEFAULT_CLIENT_VERSION));
        headersMap.put(HeadersEnum.EXECUTOR_TYPE.getKey(), String.valueOf(ExecutorTypeEnum.JAVA.getType()));
        return headersMap;
    }


    /**
     * 或者Http客户端
     *
     * @return {@link SnailHttpClient} 默认为RestTemplateClient
     */
    public static SnailHttpClient loadSnailJobHttpClient() {
        SnailJobProperties properties = SnailSpringContext.getBean(SnailJobProperties.class);
        SnailHttpClientConfig httpConfig = properties.getHttpConfig();
        if (httpConfig.getHost() == null) {
            httpConfig.setHost(properties.getServer().getHost());
        }
        return ServiceLoaderUtil.loadList(SnailHttpClient.class)
                .stream()
                .findAny()
                .orElse(new DefaultHttpClient(httpConfig));
    }

}
