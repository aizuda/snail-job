package com.aizuda.snailjob.client.core.callback.future;

import com.aizuda.snailjob.client.common.rpc.client.RequestBuilder;
import com.aizuda.snailjob.client.core.context.CallbackContext;
import com.aizuda.snailjob.client.core.client.RetryClient;
import com.aizuda.snailjob.client.model.request.RetryCallbackResultRequest;
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
            RetryCallbackResultRequest request = new RetryCallbackResultRequest();
            request.setRetryTaskId(context.getRetryTaskId());
            request.setRetryId(context.getRetryId());
            request.setResult(result);
            CLIENT.callbaclResult(request);
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
            RetryCallbackResultRequest request = new RetryCallbackResultRequest();
            request.setRetryTaskId(context.getRetryTaskId());
            request.setRetryId(context.getRetryId());
            request.setResult(Boolean.FALSE);
            CLIENT.callbaclResult(request);
        } catch (Exception e) {
            SnailJobLog.REMOTE.error("回调执行结果上报异常.[{}]", context.getRetryTaskId(), e);
        }
    }
}
