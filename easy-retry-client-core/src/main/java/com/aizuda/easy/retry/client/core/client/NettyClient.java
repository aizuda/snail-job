package com.aizuda.easy.retry.client.core.client;

import com.aizuda.easy.retry.client.core.annotation.Mapping;
import com.aizuda.easy.retry.client.core.client.netty.RequestMethod;
import com.aizuda.easy.retry.common.core.model.NettyResult;
import com.aizuda.easy.retry.common.core.model.Result;
import com.aizuda.easy.retry.server.model.dto.RetryTaskDTO;

import java.util.List;

import static com.aizuda.easy.retry.common.core.constant.SystemConstants.HTTP_PATH.BATCH_REPORT;
import static com.aizuda.easy.retry.common.core.constant.SystemConstants.HTTP_PATH.BEAT;
import static com.aizuda.easy.retry.common.core.constant.SystemConstants.HTTP_PATH.CONFIG;

/**
 * netty 客户端请求类
 *
 * @author: www.byteblogs.com
 * @date : 2023-05-11 21:28
 * @since 1.3.0
 */
public interface NettyClient {

    @Mapping(method = RequestMethod.GET, path = CONFIG)
    Result getConfig(Integer version);

    @Mapping(method = RequestMethod.GET, path = BEAT)
    Result beat(String mark);

    @Mapping(method = RequestMethod.POST, path = BATCH_REPORT)
    NettyResult reportRetryInfo(List<RetryTaskDTO> list);

}
