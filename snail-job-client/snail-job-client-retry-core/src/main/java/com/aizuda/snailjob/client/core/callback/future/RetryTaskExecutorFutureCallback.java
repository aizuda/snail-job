package com.aizuda.snailjob.client.core.callback.future;

import com.aizuda.snailjob.client.common.rpc.client.RequestBuilder;
import com.aizuda.snailjob.client.core.context.RemoteRetryContext;
import com.aizuda.snailjob.client.core.client.RetryClient;
import com.aizuda.snailjob.client.model.DispatchRetryResultDTO;
import com.aizuda.snailjob.client.model.request.DispatchRetryResultRequest;
import com.aizuda.snailjob.common.core.enums.RetryResultStatusEnum;
import com.aizuda.snailjob.common.core.enums.RetryTaskStatusEnum;
import com.aizuda.snailjob.common.core.enums.StatusEnum;
import com.aizuda.snailjob.common.core.model.SnailJobRpcResult;
import com.aizuda.snailjob.common.log.SnailJobLog;
import com.google.common.util.concurrent.FutureCallback;

import java.util.Objects;
import java.util.concurrent.CancellationException;

/**
 * <p>
 * 重试执行结果上报
 * </p>
 *
 * @author opensnail
 * @date 2025-02-11
 */
public class RetryTaskExecutorFutureCallback implements FutureCallback<DispatchRetryResultDTO> {

    private static final RetryClient CLIENT = RequestBuilder.<RetryClient, SnailJobRpcResult>newBuilder()
            .client(RetryClient.class)
            .callback(nettyResult -> {
                if (nettyResult.getStatus() == StatusEnum.NO.getStatus()) {
                    SnailJobLog.LOCAL.error("Retry execute result report successfully requestId:[{}]",
                            nettyResult.getReqId());
                }

            }).build();


    private final RemoteRetryContext retryContext;
    public RetryTaskExecutorFutureCallback(RemoteRetryContext retryContext) {
        this.retryContext = retryContext;
    }

    @Override
    public void onSuccess(DispatchRetryResultDTO result) {

        try {
            DispatchRetryResultRequest request = buildDispatchRetryResultRequest(result);
            if (RetryResultStatusEnum.SUCCESS.getStatus().equals(result.getStatusCode())) {
                request.setTaskStatus(RetryTaskStatusEnum.SUCCESS.getStatus());
            } else if (RetryResultStatusEnum.STOP.getStatus().equals(result.getStatusCode())) {
                request.setTaskStatus(RetryTaskStatusEnum.STOP.getStatus());
            } else {
                request.setTaskStatus(RetryTaskStatusEnum.FAIL.getStatus());
            }
            CLIENT.dispatchResult(request);
        } catch (Exception e) {
            SnailJobLog.REMOTE.error("执行结果上报异常.[{}]", retryContext.getRetryTaskId(), e);
        }

    }


    @Override
    public void onFailure(Throwable t) {
        if (t instanceof CancellationException) {
            SnailJobLog.LOCAL.debug("任务已经被取消，不做状态回传");
            return;
        }

        try {
            DispatchRetryResultRequest request = buildDispatchRetryResultRequest(null);
            request.setExceptionMsg(t.getMessage());
            request.setTaskStatus(RetryTaskStatusEnum.FAIL.getStatus());
            CLIENT.dispatchResult(request);
        } catch (Exception e) {
            SnailJobLog.REMOTE.error("执行结果上报异常.[{}]", retryContext.getRetryTaskId(), e);
        }

    }


    private DispatchRetryResultRequest buildDispatchRetryResultRequest(DispatchRetryResultDTO result) {
        DispatchRetryResultRequest request = new DispatchRetryResultRequest();
        request.setRetryTaskId(retryContext.getRetryTaskId());
        request.setNamespaceId(retryContext.getNamespaceId());
        request.setGroupName(retryContext.getGroupName());
        request.setSceneName(retryContext.getScene());
        request.setRetryId(retryContext.getRetryId());
        request.setRetryTaskId(retryContext.getRetryTaskId());
        if (Objects.nonNull(result)) {
            request.setResult(result.getResultJson());
            request.setExceptionMsg(result.getExceptionMsg());
        }
        return request;
    }
}
