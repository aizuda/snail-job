package com.aizuda.snailjob.server.retry.task.support.request;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.net.url.UrlQuery;
import com.aizuda.snailjob.common.core.constant.SystemConstants;
import com.aizuda.snailjob.common.core.model.SnailJobRequest;
import com.aizuda.snailjob.common.core.model.SnailJobRpcResult;
import com.aizuda.snailjob.common.core.util.JsonUtil;
import com.aizuda.snailjob.common.log.SnailJobLog;
import com.aizuda.snailjob.server.common.convert.RetryResponseVOConverter;
import com.aizuda.snailjob.server.common.exception.SnailJobServerException;
import com.aizuda.snailjob.server.common.handler.PostHttpRequestHandler;
import com.aizuda.snailjob.server.common.vo.RetryResponseVO;
import com.aizuda.snailjob.template.datasource.persistence.mapper.RetryMapper;
import com.aizuda.snailjob.template.datasource.persistence.po.Retry;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Deprecated
public class OpenApiGetRetryDetailRequestHandler extends PostHttpRequestHandler {

    private final RetryMapper retryMapper;

    @Override
    public SnailJobRpcResult doHandler(String content, UrlQuery query, HttpHeaders headers) {
        SnailJobLog.LOCAL.debug("query retry content:[{}]", content);
        SnailJobRequest retryRequest = JsonUtil.parseObject(content, SnailJobRequest.class);
        Object[] args = retryRequest.getArgs();
        Long retryId = JsonUtil.parseObject(JsonUtil.toJsonString(args[0]), Long.class);

        Retry retry = retryMapper.selectById(retryId);

        Assert.notNull(retry, () -> new SnailJobServerException("Retry task not found:[{}].", retryId));

        RetryResponseVO retryResponseVO = RetryResponseVOConverter.INSTANCE.convert(retry);
        return new SnailJobRpcResult(retryResponseVO, retryRequest.getReqId());
    }

    @Override
    public boolean supports(String path) {
        return SystemConstants.HTTP_PATH.OPENAPI_QUERY_RETRY.equals(path);
    }

    @Override
    public HttpMethod method() {
        return HttpMethod.POST;
    }
}
