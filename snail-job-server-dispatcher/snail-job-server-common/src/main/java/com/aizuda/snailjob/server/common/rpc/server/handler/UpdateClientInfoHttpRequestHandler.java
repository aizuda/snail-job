package com.aizuda.snailjob.server.common.rpc.server.handler;

import cn.hutool.core.net.url.UrlQuery;
import com.aizuda.snailjob.common.core.enums.StatusEnum;
import com.aizuda.snailjob.common.core.model.SnailJobRequest;
import com.aizuda.snailjob.common.core.model.SnailJobRpcResult;
import com.aizuda.snailjob.common.core.util.JsonUtil;
import com.aizuda.snailjob.common.log.SnailJobLog;
import com.aizuda.snailjob.server.common.dto.UpdateClientInfoDTO;
import com.aizuda.snailjob.server.common.handler.InstanceManager;
import com.aizuda.snailjob.server.common.handler.PostHttpRequestHandler;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import static com.aizuda.snailjob.common.core.constant.SystemConstants.HTTP_PATH.UPDATE_CLIENT_INFO;

/**
 * 刷新客户端信息
 */
@Component
@RequiredArgsConstructor
public class UpdateClientInfoHttpRequestHandler extends PostHttpRequestHandler {
    private final InstanceManager instanceManager;

    @Override
    public boolean supports(String path) {
        return UPDATE_CLIENT_INFO.equals(path);
    }

    @Override
    public HttpMethod method() {
        return HttpMethod.POST;
    }

    @Override
    public SnailJobRpcResult doHandler(String content, UrlQuery query, HttpHeaders headers) {
        SnailJobLog.LOCAL.info("Client Update Request. content:[{}]", content);

        SnailJobRequest retryRequest = JsonUtil.parseObject(content, SnailJobRequest.class);
        Object[] args = retryRequest.getArgs();
        UpdateClientInfoDTO clientInfoDTO = JsonUtil.parseObject(JsonUtil.toJsonString(args[0]), UpdateClientInfoDTO.class);

        try {
            Boolean updated = instanceManager.updateInstanceLabels(clientInfoDTO);
            SnailJobLog.LOCAL.info("Client Update Request. content:[{}]", content);
            return new SnailJobRpcResult(StatusEnum.YES.getStatus(), "success", updated, retryRequest.getReqId());
        } catch (Exception e) {
            return new SnailJobRpcResult(StatusEnum.YES.getStatus(), e.getMessage(), Boolean.FALSE, retryRequest.getReqId());
        }

    }
}
