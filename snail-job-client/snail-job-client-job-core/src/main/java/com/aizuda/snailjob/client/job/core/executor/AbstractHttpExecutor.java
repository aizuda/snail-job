package com.aizuda.snailjob.client.job.core.executor;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.aizuda.snailjob.client.common.config.SnailJobProperties;
import com.aizuda.snailjob.client.model.ExecuteResult;
import com.aizuda.snailjob.common.core.context.SnailSpringContext;
import com.aizuda.snailjob.common.core.exception.SnailJobInnerExecutorException;
import com.aizuda.snailjob.common.core.util.JsonUtil;
import com.aizuda.snailjob.common.log.SnailJobLog;
import lombok.Data;
import org.springframework.util.StringUtils;

import java.util.Map;


public abstract class AbstractHttpExecutor {

    private static final int DEFAULT_TIMEOUT = 60;
    public static final SnailJobProperties snailJobProperties = SnailSpringContext.getBean(SnailJobProperties.class);
    private static final String DEFAULT_REQUEST_METHOD = "GET";
    private static final String POST_REQUEST_METHOD = "POST";
    private static final String PUT_REQUEST_METHOD = "PUT";
    private static final String DELETE_REQUEST_METHOD = "DELETE";
    private static final String HTTP = "http";
    private static final String HTTP_PREFIX = "http://";
    private static final int HTTP_SUCCESS_CODE = 200;

    public ExecuteResult process(HttpParams httpParams) {
        if (httpParams == null) {
            String message = "HttpParams is null. Verify jobParam configuration.";
            logWarn(message);
            return ExecuteResult.failure(message);
        }

        // 校验url
        validateAndSetUrl(httpParams);
        // 设置默认Method及body
        setDefaultMethodAndBody(httpParams);
        setDefaultMediaType(httpParams);
        setDefaultTimeout(httpParams);
        logInfo("Request URL: {}\nUsing request method: {}\nRequest timeout: {} seconds",
                httpParams.getUrl(),
                httpParams.getMethod(),
                httpParams.getTimeout());

        HttpRequest httpRequest = buildhutoolRequest(httpParams);

        return executeRequestAndHandleResponse(httpRequest);
    }

    private ExecuteResult executeRequestAndHandleResponse(HttpRequest httpRequest) {
        try (HttpResponse response = httpRequest.execute()) {
            int errCode = response.getStatus();
            String body = response.body();
            if (errCode != HTTP_SUCCESS_CODE) {
                SnailJobLog.LOCAL.error("{} request to URL: {} failed with code: {}, response body: {}",
                        httpRequest.getMethod(), httpRequest.getUrl(), errCode, body);
                return ExecuteResult.failure("HTTP request failed");
            }
            return ExecuteResult.success(body);
        } catch (Exception e) {
            throw new SnailJobInnerExecutorException("[snail-job] HTTP internal executor failed", e);
        }
    }

    private void validateAndSetUrl(HttpParams httpParams) {
        if (StringUtils.isEmpty(httpParams.getUrl())) {
            throw new SnailJobInnerExecutorException("URL cannot be empty.");
        }
        httpParams.setUrl(httpParams.getUrl().startsWith(HTTP) ? httpParams.getUrl() : HTTP_PREFIX + httpParams.getUrl());
    }

    private void setDefaultMethodAndBody(HttpParams httpParams) {
        if (StringUtils.isEmpty(httpParams.getMethod())) {
            httpParams.setMethod(DEFAULT_REQUEST_METHOD);
        } else {
            httpParams.setMethod(httpParams.getMethod().toUpperCase());
        }

        if (!DEFAULT_REQUEST_METHOD.equals(httpParams.getMethod()) && StringUtils.isEmpty(httpParams.getBody())) {
            httpParams.setBody(JsonUtil.toJSONString());
            logWarn("Using default request body: {}", httpParams.getBody());
        }
    }

    private void setDefaultMediaType(HttpParams httpParams) {
        if (!DEFAULT_REQUEST_METHOD.equals(httpParams.getMethod()) && JsonUtil.isValidJson(httpParams.getBody()) && StringUtils.isEmpty(httpParams.getMediaType())) {
            httpParams.setMediaType("application/json");
            logWarn("Using 'application/json' as media type");
        }
    }

    private void setDefaultTimeout(HttpParams httpParams) {
        // 使用milliseconds
        httpParams.setTimeout(httpParams.getTimeout() == null ? DEFAULT_TIMEOUT * 1000 : httpParams.getTimeout() * 1000);
    }


    private HttpRequest buildhutoolRequest(HttpParams httpParams) {
        HttpRequest request;
        switch (httpParams.getMethod()) {
            case PUT_REQUEST_METHOD:
                request = HttpRequest.put(httpParams.url);
                break;
            case DELETE_REQUEST_METHOD:
                request = HttpRequest.delete(httpParams.url);
                break;
            case POST_REQUEST_METHOD:
                request = HttpRequest.post(httpParams.url);
                break;
            default:
                request = HttpRequest.get(httpParams.url);
                break;
        }

        if (httpParams.getHeaders() != null) {
            httpParams.getHeaders().forEach(request::header);
        }

        if (httpParams.getBody() != null && (httpParams.getMethod().equals(POST_REQUEST_METHOD)
                || httpParams.getMethod().equals(PUT_REQUEST_METHOD)
                || httpParams.getMethod().equals(DELETE_REQUEST_METHOD))) {
            request.body(httpParams.getBody(), httpParams.getMediaType());
        }

        request.timeout(httpParams.getTimeout());

        return request;
    }

    @Data
    public static class HttpParams {
        private String method;
        private String url;
        private String mediaType;
        private String body;
        private Map<String, String> headers;
        private Integer timeout;
    }

    // Logging methods
    private void logInfo(String msg, Object... params) {
        SnailJobLog.REMOTE.info("[snail-job] " + msg, params);
    }

    private void logWarn(String msg, Object... params) {
        SnailJobLog.REMOTE.warn("[snail-job] " + msg, params);
    }
}
