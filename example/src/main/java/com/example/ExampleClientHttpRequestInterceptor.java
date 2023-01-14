package com.example;

import com.x.retry.client.core.exception.XRetryClientException;
import com.x.retry.client.core.intercepter.RetrySiteSnapshot;
import com.x.retry.common.core.constant.SystemConstants;
import com.x.retry.common.core.log.LogUtils;
import com.x.retry.common.core.model.XRetryHeaders;
import com.x.retry.common.core.util.JsonUtil;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

/**
 * RestTemplate 拦截器
 *
 * @author: shuguang.zhang
 * @date : 2022-04-17 15:22
 */
public class ExampleClientHttpRequestInterceptor implements ClientHttpRequestInterceptor {

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {

        before(request);

        ClientHttpResponse execute = execution.execute(request, body);

        after(execute);

        return execute;
    }

    private void after(ClientHttpResponse execute) {

        HttpHeaders headers = execute.getHeaders();

        // 获取不重试标志
        if (headers.containsKey(SystemConstants.X_RETRY_STATUS_CODE_KEY)) {
            List<String> statusCode = headers.get(SystemConstants.X_RETRY_STATUS_CODE_KEY);
            RetrySiteSnapshot.setRetryStatusCode(statusCode.get(0));
        }
    }

    private void before(HttpRequest request) {

        XRetryHeaders retryHeader = RetrySiteSnapshot.getRetryHeader();

        // 传递请求头
        if (Objects.nonNull(retryHeader)) {
            long callRemoteTime = System.currentTimeMillis();
            long entryMethodTime = RetrySiteSnapshot.getEntryMethodTime();
            long transmitTime = retryHeader.getDdl() - (callRemoteTime - entryMethodTime);
            LogUtils.info("RPC传递header头 entryMethodTime:[{}] - callRemoteTime:[{}] = transmitTime:[{}]", entryMethodTime, callRemoteTime, transmitTime);
            if (transmitTime > 0) {
                retryHeader.setDdl(transmitTime);
            } else {
                throw new XRetryClientException("调用链超时, 不在继续调用后面请求");
            }

            request.getHeaders().add(SystemConstants.X_RETRY_HEAD_KEY, JsonUtil.toJsonString(retryHeader));
        }
    }
}
