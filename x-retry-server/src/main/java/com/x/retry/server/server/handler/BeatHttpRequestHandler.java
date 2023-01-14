package com.x.retry.server.server.handler;

import cn.hutool.core.net.url.UrlQuery;
import com.x.retry.common.core.model.NettyResult;
import com.x.retry.common.core.model.XRetryRequest;
import com.x.retry.common.core.util.JsonUtil;
import com.x.retry.server.support.handler.ClientRegisterHandler;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author: www.byteblogs.com
 * @date : 2022-03-07 16:26
 */
@Component
@Slf4j
public class BeatHttpRequestHandler extends GetHttpRequestHandler {

    @Autowired
    private ClientRegisterHandler clientRegisterHandler;

    private static final String URI = "/beat";

    @Override
    public boolean supports(String uri) {
        return URI.equals(uri);
    }

    @Override
    public HttpMethod method() {
        return HttpMethod.GET;
    }

    @Override
    public String doHandler(String content, UrlQuery query, HttpHeaders headers) {
        log.info("心跳检查 content:[{}]", query.toString());
        XRetryRequest retryRequest = JsonUtil.parseObject(content, XRetryRequest.class);
       return JsonUtil.toJsonString(new NettyResult("PONG", retryRequest.getRequestId()));
    }
}
