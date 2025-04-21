package com.aizuda.snailjob.client.job.core.executor.builtin;

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

import java.util.Base64;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;


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
    private static final Pattern pattern = Pattern.compile("[\\u4e00-\\u9fa5]");
    private static final int RESPONSE_SUCCESS_CODE = 200;
    private static final String RESPONSE_CODE_FIELD = "code";
    private static final String JSON_RESPONSE_TYPE = "json";
    private static final String TEXT_RESPONSE_TYPE = "text";
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
            return validateResponse(response, httpRequest, snailJobProperties.getHttpResponse());
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

    /**
     * 验证http响应是否有效，并根据响应类型进行进一步验证
     *
     * @param response
     * @param httpRequest
     * @param httpResponse
     * @return
     */
    private ExecuteResult validateResponse(HttpResponse response, HttpRequest httpRequest, SnailJobProperties.HttpResponse httpResponse) {
        int errCode = response.getStatus();
        String body = response.body();
        // 检查http响应状态码是否为成功状态码
        if (errCode != HTTP_SUCCESS_CODE) {
            SnailJobLog.LOCAL.error("{} request to URL: {} failed with code: {}, response body: {}",
                    httpRequest.getMethod(), httpRequest.getUrl(), errCode, body);
            return ExecuteResult.failure("HTTP request failed");
        }
        // 如果配置了httpResponse，则根据响应类型进行进一步验证
        if (Objects.nonNull(httpResponse)) {
            // 防止显示声明字段但是未配置值
            int code = Optional.ofNullable(httpResponse.getCode()).orElse(RESPONSE_SUCCESS_CODE);
            String field = Optional.ofNullable(httpResponse.getField())
                    .filter(StringUtils::hasLength)
                    .orElse(RESPONSE_CODE_FIELD);
            String responseType = Optional.ofNullable(httpResponse.getField())
                    .filter(StringUtils::hasLength)
                    .orElse(JSON_RESPONSE_TYPE);
            // 根据不同的响应类型进行验证
            if (JSON_RESPONSE_TYPE.equalsIgnoreCase(responseType)) {
                return validateJsonResponse(body, code, field, httpRequest);
            } else if (TEXT_RESPONSE_TYPE.equalsIgnoreCase(responseType)) {
                return validateTextResponse(body, code, httpRequest);
            } else {
                return ExecuteResult.failure("the responseType is not json or text");
            }
        }
        return ExecuteResult.success(body);
    }

    /**
     * 验证json响应类型
     *
     * @param body
     * @param code
     * @param field
     * @param httpRequest
     * @return
     */
    private ExecuteResult validateJsonResponse(String body, int code, String field, HttpRequest httpRequest) {
        // 检查响应体是否为json格式
        if (!JsonUtil.isValidJson(body) || JsonUtil.isEmptyJson(body)) {
            SnailJobLog.LOCAL.error("the responseType is json，but the response body fails to validate json or json is empty");
            return ExecuteResult.failure("the responseType is json，but the response body fails to validate json or json is empty");
        }
        // 检查响应体是否包含指定的状态码字段
        Map<String, Object> objectObjectMap = JsonUtil.parseHashMap(body);
        if (!objectObjectMap.containsKey(field)) {
            SnailJobLog.LOCAL.error("the responseType is json，but there is no status code field：" + field);
            return ExecuteResult.failure("the responseType is json，but there is no status code field：" + field);
        }
        // 检查响应体中状态码是否与指定的状态码是否一致
        if (!Objects.equals(code, objectObjectMap.get(field))) {
            SnailJobLog.LOCAL.error("{} request to URL: {} failed with code: {}, response body: {}",
                    httpRequest.getMethod(), httpRequest.getUrl(), code, body);
            return ExecuteResult.failure("the response status code is not equal to the specified status code");
        }
        return ExecuteResult.success(body);
    }

    /**
     * 验证text响应类型
     *
     * @param body
     * @param code
     * @param httpRequest
     * @return
     */
    private ExecuteResult validateTextResponse(String body, int code, HttpRequest httpRequest) {
        // 检查响应体是否为空
        if (!StringUtils.hasLength(body)) {
            SnailJobLog.LOCAL.error("the responseType is text，but the response body is empty");
            return ExecuteResult.failure("the responseType is text，but the response body is empty");
        }
        // 检查响应体是否与指定的状态码是否一致
        if (!Objects.equals(code + "", body)) {
            SnailJobLog.LOCAL.error("{} request to URL: {} failed with code: {}, response body: {}",
                    httpRequest.getMethod(), httpRequest.getUrl(), code, body);
            return ExecuteResult.failure("the response status code is not equal to the specified status code");
        }
        return ExecuteResult.success(body);
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
        httpParams.setTimeout(Objects.isNull(httpParams.getTimeout()) ? DEFAULT_TIMEOUT * 1000 : httpParams.getTimeout() * 1000);
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

        if (Objects.nonNull(httpParams.getHeaders())) {
            httpParams.getHeaders().forEach(request::header);
        }
        // 有上下文时，在请求中透传上下文;即工作流中支持上下文的传递
        if (Objects.nonNull(httpParams.getWfContext())) {
            httpParams.getWfContext().forEach((key, value) -> {
                String headerValue = (value instanceof String) ? (String) value : JsonUtil.toJsonString(value);
                // 正则表达式匹配中文字符
                if (pattern.matcher(headerValue).find()) {
                    // 如果包含中文字符，则进行Base64编码
                    headerValue = Base64.getEncoder().encodeToString(headerValue.getBytes());
                }
                request.header(key, headerValue);
            });
        }

        if (Objects.nonNull(httpParams.getBody()) && (httpParams.getMethod().equals(POST_REQUEST_METHOD)
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
        private Map<String, Object> wfContext;
    }

    // Logging methods
    private void logInfo(String msg, Object... params) {
        SnailJobLog.REMOTE.info("[snail-job] " + msg, params);
    }

    private void logWarn(String msg, Object... params) {
        SnailJobLog.REMOTE.warn("[snail-job] " + msg, params);
    }
}
