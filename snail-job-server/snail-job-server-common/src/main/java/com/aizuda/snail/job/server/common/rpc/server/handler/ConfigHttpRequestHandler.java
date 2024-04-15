package com.aizuda.snail.job.server.common.rpc.server.handler;

import cn.hutool.core.net.url.UrlQuery;
import com.aizuda.snail.job.common.log.EasyRetryLog;
import com.aizuda.snail.job.server.common.handler.GetHttpRequestHandler;
import com.aizuda.snail.job.server.model.dto.ConfigDTO;
import com.aizuda.snail.job.common.core.enums.HeadersEnum;
import com.aizuda.snail.job.common.core.model.NettyResult;
import com.aizuda.snail.job.common.core.model.EasyRetryRequest;
import com.aizuda.snail.job.common.core.util.JsonUtil;
import com.aizuda.snail.job.template.datasource.access.AccessTemplate;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import static com.aizuda.snail.job.common.core.constant.SystemConstants.HTTP_PATH.CONFIG;

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
        return CONFIG.equals(path);
    }

    @Override
    public HttpMethod method() {
        return HttpMethod.GET;
    }

    @Override
    public String doHandler(String content, UrlQuery urlQuery, HttpHeaders headers) {
        EasyRetryLog.LOCAL.info("版本同步 content:[{}]", urlQuery.toString());
        EasyRetryRequest retryRequest = JsonUtil.parseObject(content, EasyRetryRequest.class);
        String groupName = headers.get(HeadersEnum.GROUP_NAME.getKey());
        String namespace = headers.get(HeadersEnum.NAMESPACE.getKey());
        ConfigDTO configDTO = accessTemplate.getGroupConfigAccess().getConfigInfo(groupName, namespace);
        return JsonUtil.toJsonString(new NettyResult(JsonUtil.toJsonString(configDTO), retryRequest.getReqId()));
    }
}
