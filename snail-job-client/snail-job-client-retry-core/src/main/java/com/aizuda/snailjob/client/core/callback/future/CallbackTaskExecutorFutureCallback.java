package com.aizuda.snailjob.client.core.callback.future;

import com.aizuda.snailjob.client.common.rpc.client.RequestBuilder;
import com.aizuda.snailjob.client.core.context.CallbackContext;
import com.aizuda.snailjob.client.core.client.RetryClient;
import com.aizuda.snailjob.client.model.request.DispatchCallbackResultRequest;
import com.aizuda.snailjob.common.core.enums.RetryTaskStatusEnum;
import com.aizuda.snailjob.common.core.enums.StatusEnum;
import com.aizuda.snailjob.common.core.model.SnailJobRpcResult;
import com.aizuda.snailjob.common.log.SnailJobLog;
import com.google.common.util.concurrent.FutureCallback;

import java.util.concurrent.CancellationException;

/**
 * <p>
 *
 * </p>
 *
 * @author opensnail
 * @date 2025-02-11
 */
public class CallbackTaskExecutorFutureCallback implements FutureCallback<Boolean> {

    private static final RetryClient CLIENT = RequestBuilder.<RetryClient, SnailJobRpcResult>newBuilder()
            .client(RetryClient.class)
            .callback(nettyResult -> {
                if (nettyResult.getStatus() == StatusEnum.NO.getStatus()) {
                    SnailJobLog.LOCAL.error("Retry callback execute result report successfully requestId:[{}]",
                            nettyResult.getReqId());
                }

            }).build();


    private final CallbackContext context;
    public CallbackTaskExecutorFutureCallback(CallbackContext context) {
        this.context = context;
    }

    @Override
    public void onSuccess(Boolean result) {
        try {
            DispatchCallbackResultRequest request = buildDispatchRetryResultRequest();
            request.setTaskStatus(RetryTaskStatusEnum.SUCCESS.getStatus());
            CLIENT.callbackResult(request);
        } catch (Exception e) {
            SnailJobLog.REMOTE.error("回调执行结果上报异常.[{}]", context.getRetryTaskId(), e);

        }

    }

    @Override
    public void onFailure(Throwable t) {
        if (t instanceof CancellationException) {
            SnailJobLog.LOCAL.debug("任务已经被取消，不做状态回传");
            return;
        }
        try {
            DispatchCallbackResultRequest request = buildDispatchRetryResultRequest();
            request.setTaskStatus(RetryTaskStatusEnum.FAIL.getStatus());
            request.setExceptionMsg(t.getMessage());
            CLIENT.callbackResult(request);
        } catch (Exception e) {
            SnailJobLog.REMOTE.error("回调执行结果上报异常.[{}]", context.getRetryTaskId(), e);
        }
    }

    private DispatchCallbackResultRequest buildDispatchRetryResultRequest() {
        DispatchCallbackResultRequest request = new DispatchCallbackResultRequest();
        request.setRetryTaskId(context.getRetryTaskId());
        request.setNamespaceId(context.getNamespaceId());
        request.setGroupName(context.getGroupName());
        request.setSceneName(context.getSceneName());
        request.setRetryId(context.getRetryId());
        request.setRetryTaskId(context.getRetryTaskId());
        return request;
    }
}
