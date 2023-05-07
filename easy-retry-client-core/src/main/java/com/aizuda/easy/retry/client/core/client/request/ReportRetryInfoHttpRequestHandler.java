package com.aizuda.easy.retry.client.core.client.request;

import cn.hutool.core.net.url.UrlBuilder;
import cn.hutool.core.net.url.UrlQuery;
import com.aizuda.easy.retry.common.core.log.LogUtils;
import com.aizuda.easy.retry.common.core.model.NettyResult;
import com.aizuda.easy.retry.common.core.model.EasyRetryRequest;
import com.aizuda.easy.retry.common.core.util.JsonUtil;
import io.netty.handler.codec.http.HttpMethod;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

/**
 * @author: www.byteblogs.com
 * @date : 2022-03-07 16:39
 */
@Component
@Slf4j
public class ReportRetryInfoHttpRequestHandler extends PostHttpRequestHandler {

    private static final String URI = "/batch/report";

    @Override
    public HttpMethod method() {
        return HttpMethod.POST;
    }

    @Override
    public String getHttpUrl(RequestParam requestParam) {
        UrlBuilder urlBuilder = UrlBuilder.ofHttp(URI);
        urlBuilder.setQuery(new UrlQuery().addAll(JsonUtil.parseHashMap(JsonUtil.toJsonString(requestParam))));
        return urlBuilder.toURI().toASCIIString();
    }

    @Override
    public Consumer<NettyResult> callable() {
        return nettyResult -> LogUtils.info(log, "同步回调");
    }

    @Override
    public  String body(EasyRetryRequest retryRequest) {
        return JsonUtil.toJsonString(retryRequest);
    }
}
