package com.aizuda.snailjob.client.common;

import com.aizuda.snailjob.client.common.annotation.Mapping;
import com.aizuda.snailjob.client.common.rpc.client.RequestMethod;
import com.aizuda.snailjob.common.core.constant.SystemConstants.HTTP_PATH;
import com.aizuda.snailjob.common.core.model.Result;


/**
 * netty 客户端请求类
 *
 * @author: opensnail
 * @date : 2023-05-11 21:28
 * @since 1.3.0
 */
public interface NettyClient {

    @Mapping(method = RequestMethod.GET, path = HTTP_PATH.SYNC_CONFIG)
    Result syncRemoteConfig();

    @Mapping(method = RequestMethod.GET, path = HTTP_PATH.BEAT)
    Result beat(String mark);


}
