package com.example;

import com.x.retry.client.core.intercepter.RetrySiteSnapshot;
import com.x.retry.common.core.constant.SystemConstants;
import com.x.retry.common.core.model.XRetryHeaders;
import com.x.retry.common.core.util.JsonUtil;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;
import java.util.Objects;

/**
 * @author: shuguang.zhang
 * @date : 2022-04-17 15:22
 */
public class ExampleClientHttpRequestInterceptor implements ClientHttpRequestInterceptor {

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        XRetryHeaders retryHeader = RetrySiteSnapshot.getRetryHeader();
        if (Objects.nonNull(retryHeader)) {
            request.getHeaders().add(SystemConstants.X_RETRY_HEAD, JsonUtil.toJsonString(retryHeader));
        }

        return execution.execute(request, body);
    }
}
