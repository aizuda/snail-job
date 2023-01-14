package com.x.retry.client.core.client.request;

import cn.hutool.core.net.url.UrlBuilder;
import cn.hutool.core.net.url.UrlQuery;
import com.x.retry.client.core.cache.GroupVersionCache;
import com.x.retry.common.core.log.LogUtils;
import com.x.retry.common.core.model.NettyResult;
import com.x.retry.common.core.model.XRetryRequest;
import com.x.retry.common.core.util.JsonUtil;
import com.x.retry.server.model.dto.ConfigDTO;
import io.netty.handler.codec.http.HttpMethod;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * @author: www.byteblogs.com
 * @date : 2022-03-07 16:29
 */
@Component
@Slf4j
public class ConfigHttpRequestHandler extends GetHttpRequestHandler {

    private static final String URI = "/config";

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
            if (Objects.isNull(nettyResult.getData())) {
                LogUtils.error("获取配置结果为null");
                return;
            }

            GroupVersionCache.configDTO = JsonUtil.parseObject(nettyResult.getData().toString(), ConfigDTO.class);
        };
    }

    @Override
    public  String body(XRetryRequest retryRequest) {
        return JsonUtil.toJsonString(retryRequest);
    }
}
