package com.aizuda.snailjob.client.common.rpc.client.openapi;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpUtil;
import cn.hutool.http.Method;
import com.aizuda.snailjob.client.common.config.SnailJobProperties;
import com.aizuda.snailjob.common.core.context.SnailSpringContext;
import com.aizuda.snailjob.common.core.model.SnailJobOpenApiResult;
import com.aizuda.snailjob.common.core.util.JsonUtil;

import java.text.MessageFormat;

/**
 * <p>
 * 默认用DefaultHttpClient 支持扩展其他HttpClient
 * </p>
 *
 * @author opensnail
 * @date 2025-07-05
 */
public class DefaultHttpClient implements SnailHttpClient {
    private final static String URL = "{0}://{1}:{2,number,#}/openapi/{3}";
    private final SnailHttpClientConfig config;

    public DefaultHttpClient(SnailHttpClientConfig config) {
        this.config = config;
    }

    @Override
    public SnailJobOpenApiResult execute(Request request) {
        String path = request.getPath();
        if (path.startsWith("/")) {
            path = path.substring(1);
        }

        String host = config.getHost();
        if (StrUtil.isBlank(host)) {
            SnailJobProperties properties = SnailSpringContext.getBean(SnailJobProperties.class);
            host = properties.getHost();
        }

        String url = MessageFormat.format(URL, config.isHttps() ? "https" : "http", host, config.getPort(), path);
        HttpRequest httpRequest = HttpUtil.createRequest(Method.valueOf(request.getMethod()), url);
        return httpRequest.thenFunction(httpResponse ->
                JsonUtil.parseObject(httpResponse.body(), SnailJobOpenApiResult.class));
    }
}
