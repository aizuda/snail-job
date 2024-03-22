package com.aizuda.easy.retry.client.common.log.report;

import com.aizuda.easy.retry.client.common.client.NettyClient;
import com.aizuda.easy.retry.client.common.proxy.RequestBuilder;
import com.aizuda.easy.retry.common.log.EasyRetryLog;
import com.aizuda.easy.retry.common.core.model.NettyResult;
import com.aizuda.easy.retry.common.core.window.Listener;
import com.aizuda.easy.retry.server.model.dto.LogTaskDTO;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * 批量异步上报
 *
 * @author: www.byteblogs.com
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
