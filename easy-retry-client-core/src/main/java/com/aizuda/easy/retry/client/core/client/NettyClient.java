package com.aizuda.easy.retry.client.core.client;

import com.aizuda.easy.retry.client.core.annotation.Mapping;
import com.aizuda.easy.retry.client.core.client.netty.RequestMethod;
import com.aizuda.easy.retry.common.core.model.NettyResult;
import com.aizuda.easy.retry.common.core.model.Result;
import com.aizuda.easy.retry.server.model.dto.RetryTaskDTO;

import java.util.List;

/**
 * netty 客户端请求服务端
 *
 * @author: www.byteblogs.com
 * @date : 2023-05-11 21:28
 * @since 1.3.0
 */
public interface NettyClient {

    @Mapping(method = RequestMethod.GET, path = "/config")
    Result getConfig(Integer version);

    @Mapping(method = RequestMethod.GET, path = "/beat")
    Result beat(String mark);

    @Mapping(method = RequestMethod.POST, path = "/batch/report")
    NettyResult reportRetryInfo(List<RetryTaskDTO> list);

}
