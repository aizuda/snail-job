package com.aizuda.easy.retry.server.server.handler;

import cn.hutool.core.net.url.UrlQuery;
import com.aizuda.easy.retry.server.model.dto.ConfigDTO;
import com.aizuda.easy.retry.server.persistence.support.ConfigAccess;
import com.aizuda.easy.retry.common.core.enums.HeadersEnum;
import com.aizuda.easy.retry.common.core.model.NettyResult;
import com.aizuda.easy.retry.common.core.model.EasyRetryRequest;
import com.aizuda.easy.retry.common.core.util.JsonUtil;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import static com.aizuda.easy.retry.common.core.constant.SystemConstants.HTTP_PATH.CONFIG;

/**
 * @author: www.byteblogs.com
 * @date : 2022-03-07 16:29
 * @since 1.0.0
 */
@Component
@Slf4j
public class ConfigHttpRequestHandler extends GetHttpRequestHandler {

    @Autowired
    @Qualifier("configAccessProcessor")
    private ConfigAccess configAccess;

    @Override
    public boolean supports(String path) {
        return CONFIG.equals(path);
    }

    @Override
    public HttpMethod method() {
        return HttpMethod.GET;
    }

    @Override
    public String doHandler(String content, UrlQuery urlQuery, HttpHeaders headers) {
        log.info("版本同步 content:[{}]", urlQuery.toString());
        EasyRetryRequest retryRequest = JsonUtil.parseObject(content, EasyRetryRequest.class);
        String groupName = headers.get(HeadersEnum.GROUP_NAME.getKey());
        ConfigDTO configDTO = configAccess.getConfigInfo(groupName);
        return JsonUtil.toJsonString(new NettyResult(JsonUtil.toJsonString(configDTO), retryRequest.getReqId()));
    }
}
