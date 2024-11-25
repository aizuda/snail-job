package com.aizuda.snailjob.server.job.task.support.request;

import cn.hutool.core.net.url.UrlQuery;
import com.aizuda.snailjob.client.model.request.DispatchJobResultRequest;
import com.aizuda.snailjob.common.core.enums.HeadersEnum;
import com.aizuda.snailjob.common.core.enums.StatusEnum;
import com.aizuda.snailjob.common.core.model.SnailJobRpcResult;
import com.aizuda.snailjob.common.core.model.SnailJobRequest;
import com.aizuda.snailjob.common.core.util.JsonUtil;
import com.aizuda.snailjob.common.log.SnailJobLog;
import com.aizuda.snailjob.server.common.handler.PostHttpRequestHandler;
import com.aizuda.snailjob.server.job.task.support.ClientCallbackHandler;
import com.aizuda.snailjob.server.job.task.support.JobTaskConverter;
import com.aizuda.snailjob.server.job.task.support.callback.ClientCallbackContext;
import com.aizuda.snailjob.server.job.task.support.callback.ClientCallbackFactory;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import org.springframework.stereotype.Component;

import java.util.Objects;

import static com.aizuda.snailjob.common.core.constant.SystemConstants.HTTP_PATH.REPORT_JOB_DISPATCH_RESULT;

/**
 * @author opensnail
 * @date 2023-09-30 23:01:58
 * @since 2.4.0
 */
@Component
public class ReportDispatchResultPostHttpRequestHandler extends PostHttpRequestHandler {

    @Override
    public boolean supports(String path) {
        return REPORT_JOB_DISPATCH_RESULT.equals(path);
    }

    @Override
    public HttpMethod method() {
        return HttpMethod.POST;
    }

    @Override
    public SnailJobRpcResult doHandler(String content, UrlQuery query, HttpHeaders headers) {
        SnailJobLog.LOCAL.debug("Client Callback Request. content:[{}]", content);

        SnailJobRequest retryRequest = JsonUtil.parseObject(content, SnailJobRequest.class);
        Object[] args = retryRequest.getArgs();

        DispatchJobResultRequest dispatchJobResultRequest = JsonUtil.parseObject(JsonUtil.toJsonString(args[0]), DispatchJobResultRequest.class);

        ClientCallbackHandler clientCallback = ClientCallbackFactory.getClientCallback(dispatchJobResultRequest.getTaskType());

        ClientCallbackContext context = JobTaskConverter.INSTANCE.toClientCallbackContext(dispatchJobResultRequest);
        // 兼容过度版本
        if (Objects.isNull(context.getRetryStatus())) {
            context.setRetryStatus(context.isRetry());
        }
        context.setNamespaceId(headers.getAsString(HeadersEnum.NAMESPACE.getKey()));
        clientCallback.callback(context);

        return new SnailJobRpcResult(StatusEnum.YES.getStatus(), "Report Dispatch Result Processed Successfully", Boolean.TRUE, retryRequest.getReqId());
    }
}
