package com.aizuda.snailjob.client.common.log.report;

import com.aizuda.snailjob.client.common.NettyClient;
import com.aizuda.snailjob.client.common.rpc.client.RequestBuilder;
import com.aizuda.snailjob.common.core.model.SnailJobRpcResult;
import com.aizuda.snailjob.common.core.window.Listener;
import com.aizuda.snailjob.common.log.SnailJobLog;
import com.aizuda.snailjob.server.model.dto.LogTaskDTO;
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
public class ReportLogListener implements Listener<LogTaskDTO> {

    private static final NettyClient CLIENT = RequestBuilder.<NettyClient, SnailJobRpcResult>newBuilder()
            .client(NettyClient.class)
            .callback(nettyResult -> SnailJobLog.LOCAL.info("Data report log successfully requestId:[{}]", nettyResult.getReqId())).build();

    @Override
    public void handler(List<LogTaskDTO> list) {

        CLIENT.reportLogTask(list);
    }
}
