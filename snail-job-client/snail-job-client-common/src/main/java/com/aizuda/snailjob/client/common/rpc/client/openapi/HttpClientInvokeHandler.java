package com.aizuda.snailjob.client.common.rpc.client.openapi;

import cn.hutool.core.util.ServiceLoaderUtil;
import com.aizuda.snailjob.client.common.annotation.Header;
import com.aizuda.snailjob.client.common.annotation.Mapping;
import com.aizuda.snailjob.common.core.model.Result;
import com.aizuda.snailjob.common.core.model.SnailJobOpenApiResult;
import com.aizuda.snailjob.common.core.util.JsonUtil;
import lombok.AllArgsConstructor;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
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
public class HttpClientInvokeHandler <R extends Result<Object>> implements InvocationHandler {
    private final long timeout;
    private final TimeUnit unit;

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        SnailHttpClient snailHttpClient = loadSnailJobHttpClient();
        Mapping mapping = method.getAnnotation(Mapping.class);

        Request request = new Request();
        request.setMethod(mapping.method().name());
        request.setPath(mapping.path());
        request.setHeaders(getHeaderInfo(method, args));
        request.setTimeout(unit.toMillis(timeout));
        request.setBody(JsonUtil.toJsonString(args));

        return snailHttpClient.execute(request);
    }

    private Map<String, Object> getHeaderInfo(Method method, Object[] args) {
        Map<String, Object> map = new HashMap<>();
        Parameter[] parameters = method.getParameters();
        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            if (parameter.isAnnotationPresent(Header.class)) {
                Header header = parameter.getAnnotation(Header.class);
                Object o = args[i];
                if (Objects.nonNull(o)) {
                    map.put(header.name().getKey(), JsonUtil.toJsonString(o));
                }
            }
        }
        return map;
    }


    /**
     * 或者Http客户端
     *
     * @return {@link SnailHttpClient} 默认为RestTemplateClient
     */
    public static SnailHttpClient loadSnailJobHttpClient() {
        return ServiceLoaderUtil.loadList(SnailHttpClient.class)
                .stream()
                .findAny()
                .orElse(new DefaultHttpClient(null));
    }

}
