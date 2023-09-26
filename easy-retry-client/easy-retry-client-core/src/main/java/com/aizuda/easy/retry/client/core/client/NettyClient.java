package com.aizuda.easy.retry.client.core.client;

import com.aizuda.easy.retry.common.core.constant.SystemConstants.HTTP_PATH;
import com.aizuda.easy.retry.common.core.model.NettyResult;
import com.aizuda.easy.retry.common.core.model.Result;
import com.aizuda.easy.retry.server.model.dto.RetryTaskDTO;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;


/**
 * netty 客户端请求类
 *
 * @author: www.byteblogs.com
 * @date : 2023-05-11 21:28
 * @since 1.3.0
 */
public interface NettyClient {

    @Mapping(method = RequestMethod.GET, path = HTTP_PATH.CONFIG)
    Result getConfig(Integer version);

    @Mapping(method = RequestMethod.POST, path = HTTP_PATH.BATCH_REPORT)
    NettyResult reportRetryInfo(List<RetryTaskDTO> list);

}
