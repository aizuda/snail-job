package com.x.retry.server.server.handler;

import cn.hutool.core.net.url.UrlQuery;
import com.x.retry.common.core.model.Result;
import com.x.retry.common.core.util.JsonUtil;
import com.x.retry.server.model.dto.ConfigDTO;
import com.x.retry.server.service.GroupConfigService;
import io.netty.handler.codec.http.HttpMethod;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author: www.byteblogs.com
 * @date : 2022-03-07 16:29
 */
@Component
@Slf4j
public class ConfigHttpRequestHandler extends GetHttpRequestHandler {

    @Autowired
    private GroupConfigService groupConfigService;

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
    public String doHandler(String content, UrlQuery urlQuery) {
        log.info("版本同步 content:[{}]", urlQuery.toString());
        String groupName = String.valueOf(urlQuery.get("groupName"));
        ConfigDTO configDTO = groupConfigService.syncConfig(groupName);
        return JsonUtil.toJsonString(new Result<>(configDTO));
    }
}
