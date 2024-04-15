package com.aizuda.snail.job.client.common;

import com.aizuda.snail.job.client.common.annotation.Mapping;
import com.aizuda.snail.job.client.common.rpc.client.RequestMethod;
import com.aizuda.snail.job.common.core.constant.SystemConstants.HTTP_PATH;
import com.aizuda.snail.job.common.core.model.Result;


/**
 * netty 客户端请求类
 *
 * @author: opensnail
 * @date : 2023-05-11 21:28
 * @since 1.3.0
 */
public interface NettyClient {

    @Mapping(method = RequestMethod.GET, path = HTTP_PATH.CONFIG)
    Result getConfig(Integer version);

    @Mapping(method = RequestMethod.GET, path = HTTP_PATH.BEAT)
    Result beat(String mark);


}
