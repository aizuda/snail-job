package com.aizuda.snailjob.client.common.rpc.client.openapi;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpUtil;
import cn.hutool.http.Method;
import com.aizuda.snailjob.client.common.config.SnailJobProperties;
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
    private final static String URL = "{0}://{1}:{2,number,#}/{3}/{4}";
    private final static String HTTPS = "https";
    private final static String HTTP = "http";
    private final SnailJobProperties.SnailOpenApiConfig config;

    public DefaultHttpClient(SnailJobProperties.SnailOpenApiConfig config) {
        this.config = config;
    }

    @Override
    public SnailJobOpenApiResult execute(Request request) {
        String path = request.getPath();
        if (path.startsWith(StrUtil.SLASH)) {
            path = path.substring(1);
        }

        String prefix = config.getPrefix();
        if (prefix.startsWith(StrUtil.SLASH)) {
            prefix = prefix.substring(1);
        }

        String host = config.getHost();

        String url = MessageFormat.format(URL, config.isHttps() ? HTTPS : HTTP, host, config.getPort(), prefix, path);
        if (StrUtil.isNotBlank(request.getParams())){
            url += request.getParams();
        }
        HttpRequest httpRequest = HttpUtil.createRequest(Method.valueOf(request.getMethod()), url);
        httpRequest.body(request.getBody());
        httpRequest.addHeaders(request.getHeaders());
        return httpRequest.thenFunction(httpResponse ->
                JsonUtil.parseObject(httpResponse.body(), SnailJobOpenApiResult.class));
    }
}
