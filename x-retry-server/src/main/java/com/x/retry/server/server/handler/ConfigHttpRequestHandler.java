package com.x.retry.server.server.handler;

import cn.hutool.core.net.url.UrlQuery;
import com.x.retry.common.core.enums.HeadersEnum;
import com.x.retry.common.core.model.NettyResult;
import com.x.retry.common.core.model.XRetryRequest;
import com.x.retry.common.core.util.JsonUtil;
import com.x.retry.server.model.dto.ConfigDTO;
import com.x.retry.server.persistence.support.ConfigAccess;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * @author: www.byteblogs.com
 * @date : 2022-03-07 16:29
 */
@Component
@Slf4j
public class ConfigHttpRequestHandler extends GetHttpRequestHandler {

    @Autowired
    @Qualifier("configAccessProcessor")
    private ConfigAccess configAccess;

    private static final String URI = "/config";

    @Override
    public boolean supports(String uri) {
        return URI.equals(uri);
    }

    @Override
    public HttpMethod method() {
        return HttpMethod.GET;
    }

    @Override
    public String doHandler(String content, UrlQuery urlQuery, HttpHeaders headers) {
        log.info("版本同步 content:[{}]", urlQuery.toString());
        XRetryRequest retryRequest = JsonUtil.parseObject(content, XRetryRequest.class);
        String groupName = headers.get(HeadersEnum.GROUP_NAME.getKey());
        ConfigDTO configDTO = configAccess.getConfigInfo(groupName);
        return JsonUtil.toJsonString(new NettyResult(JsonUtil.toJsonString(configDTO), retryRequest.getRequestId()));
    }
}
