package com.aizuda.snail.job.client.common.client;

import com.aizuda.snail.job.client.common.annotation.Mapping;
import com.aizuda.snail.job.client.common.rpc.client.RequestMethod;
import com.aizuda.snail.job.common.core.constant.SystemConstants.HTTP_PATH;
import com.aizuda.snail.job.common.core.model.NettyResult;
import com.aizuda.snail.job.server.model.dto.LogTaskDTO;
import com.aizuda.snail.job.server.model.dto.RetryTaskDTO;

import java.util.List;


/**
 * netty 客户端请求类
 *
 * @author: opensnail
 * @date : 2023-05-11 21:28
 * @since 1.3.0
 */
public interface NettyClient {

    @Mapping(method = RequestMethod.POST, path = HTTP_PATH.BATCH_REPORT)
    NettyResult reportRetryInfo(List<RetryTaskDTO> list);

    @Mapping(method = RequestMethod.POST, path = HTTP_PATH.BATCH_LOG_REPORT)
    NettyResult reportLogTask(List<LogTaskDTO> list);

}
