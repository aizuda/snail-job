package com.aizuda.snailjob.server.common.rpc.server.handler;

import cn.hutool.core.net.url.UrlQuery;
import com.aizuda.snailjob.common.core.constant.SystemConstants.HTTP_PATH;
import com.aizuda.snailjob.common.core.enums.HeadersEnum;
import com.aizuda.snailjob.common.core.model.SnailJobRpcResult;
import com.aizuda.snailjob.common.core.model.SnailJobRequest;
import com.aizuda.snailjob.common.core.util.JsonUtil;
import com.aizuda.snailjob.server.common.handler.GetHttpRequestHandler;
import com.aizuda.snailjob.server.model.dto.ConfigDTO;
import com.aizuda.snailjob.template.datasource.access.AccessTemplate;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * @author: opensnail
 * @date : 2022-03-07 16:29
 * @since 1.0.0
 */
@Component
@RequiredArgsConstructor
public class ConfigHttpRequestHandler extends GetHttpRequestHandler {
    private final AccessTemplate accessTemplate;

    @Override
    public boolean supports(String path) {
        return HTTP_PATH.SYNC_CONFIG.equals(path);
    }

    @Override
    public HttpMethod method() {
        return HttpMethod.POST;
    }

    @Override
    public SnailJobRpcResult doHandler(String content, UrlQuery urlQuery, HttpHeaders headers) {
        SnailJobRequest retryRequest = JsonUtil.parseObject(content, SnailJobRequest.class);
        String groupName = headers.get(HeadersEnum.GROUP_NAME.getKey());
        String namespace = headers.get(HeadersEnum.NAMESPACE.getKey());
        ConfigDTO configDTO = accessTemplate.getGroupConfigAccess().getConfigInfo(groupName, namespace);
        return new SnailJobRpcResult(JsonUtil.toJsonString(configDTO), retryRequest.getReqId());
    }
}
