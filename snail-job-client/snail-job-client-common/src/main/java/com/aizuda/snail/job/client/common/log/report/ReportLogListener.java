package com.aizuda.snail.job.client.common.log.report;

import com.aizuda.snail.job.client.common.client.NettyClient;
import com.aizuda.snail.job.client.common.rpc.client.RequestBuilder;
import com.aizuda.snail.job.common.log.EasyRetryLog;
import com.aizuda.snail.job.common.core.model.NettyResult;
import com.aizuda.snail.job.common.core.window.Listener;
import com.aizuda.snail.job.server.model.dto.LogTaskDTO;
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

    private static final NettyClient CLIENT = RequestBuilder.<NettyClient, NettyResult>newBuilder()
            .client(NettyClient.class)
            .callback(nettyResult ->EasyRetryLog.LOCAL.info("Data report log successfully requestId:[{}]", nettyResult.getRequestId())).build();

    @Override
    public void handler(List<LogTaskDTO> list) {

        CLIENT.reportLogTask(list);
    }
}
