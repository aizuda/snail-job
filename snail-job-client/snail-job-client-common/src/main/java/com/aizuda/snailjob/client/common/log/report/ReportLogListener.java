package com.aizuda.snailjob.client.common.log.report;

import com.aizuda.snailjob.client.common.RpcClient;
import com.aizuda.snailjob.client.common.rpc.client.RequestBuilder;
import com.aizuda.snailjob.model.request.LogTaskRequest;
import com.aizuda.snailjob.common.core.model.SnailJobRpcResult;
import com.aizuda.snailjob.common.core.window.Listener;
import com.aizuda.snailjob.common.log.SnailJobLog;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * 批量异步上报
 *
 * @author: opensnail
 * @date : 2022-03-08 13:54
 * @since 1.0.0
 */
@Slf4j
public class ReportLogListener implements Listener<LogTaskRequest> {

    private static final RpcClient CLIENT = RequestBuilder.<RpcClient, SnailJobRpcResult>newBuilder()
            .client(RpcClient.class)
            .callback(rpcResult -> SnailJobLog.LOCAL.info("Data report log successfully requestId:[{}]",
                    rpcResult.getReqId())).build();

    @Override
    public void handler(List<LogTaskRequest> list) {

        CLIENT.reportLogTask(list);
    }
}
