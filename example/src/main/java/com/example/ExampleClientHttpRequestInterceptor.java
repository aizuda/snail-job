package com.example;

import com.aizuda.easy.retry.client.core.plugin.RequestHeaderPlugins;
import com.aizuda.easy.retry.client.core.plugin.ResponseHeaderPlugins;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;
import java.util.Map;

/**
 * RestTemplate 拦截器
 *
 * @author: www.byteblogs.com
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
        ResponseHeaderPlugins.responseHeader(execute.getHeaders());
    }

    private void before(HttpRequest request) {

        Map<String, String> header = RequestHeaderPlugins.requestHeader();
        HttpHeaders headers = request.getHeaders();
        header.forEach((key, value) -> headers.add(key, value));

    }
}
