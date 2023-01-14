package com.x.retry.client.core.client.request;

import cn.hutool.core.net.url.UrlBuilder;
import cn.hutool.core.net.url.UrlQuery;
import com.x.retry.common.core.model.NettyResult;
import com.x.retry.common.core.model.XRetryRequest;
import com.x.retry.common.core.util.JsonUtil;
import io.netty.handler.codec.http.HttpMethod;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

/**
 * @author: www.byteblogs.com
 * @date : 2022-03-07 16:26
 */
@Component
@Slf4j
public class BeatHttpRequestHandler extends GetHttpRequestHandler {

    public static final String URI = "/beat";

    @Override
    public HttpMethod method() {
        return HttpMethod.GET;
    }

    @Override
    public String getHttpUrl(RequestParam requestParam) {
        UrlBuilder urlBuilder = UrlBuilder.ofHttp(URI);
        urlBuilder.setQuery(new UrlQuery().addAll(JsonUtil.parseHashMap(JsonUtil.toJsonString(requestParam))));
        return urlBuilder.toURI().toASCIIString();
    }

    @Override
    public Consumer<NettyResult> callable() {
        return nettyResult -> {
            log.info("心跳检查 nettyResult:[{}]", JsonUtil.toJsonString(nettyResult));
        };
    }

    @Override
    public String body(XRetryRequest retryRequest) {
        return JsonUtil.toJsonString(retryRequest);
    }

}
