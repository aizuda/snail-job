package com.aizuda.snailjob.client.common;

import com.aizuda.snailjob.client.common.annotation.Mapping;
import com.aizuda.snailjob.client.common.rpc.client.RequestMethod;
import com.aizuda.snailjob.common.core.constant.SystemConstants.HTTP_PATH;
import com.aizuda.snailjob.common.core.model.SnailJobRpcResult;
import com.aizuda.snailjob.common.core.model.Result;
import com.aizuda.snailjob.server.model.dto.LogTaskDTO;
import com.aizuda.snailjob.server.model.dto.RetryTaskDTO;

import java.util.List;


/**
 * rpc 客户端请求类
 *
 * @author: opensnail
 * @date : 2023-05-11 21:28
 * @since 1.3.0
 */
public interface RpcClient {

    @Mapping(method = RequestMethod.POST, path = HTTP_PATH.BATCH_REPORT)
    SnailJobRpcResult reportRetryInfo(List<RetryTaskDTO> list);

    @Mapping(method = RequestMethod.POST, path = HTTP_PATH.BATCH_LOG_REPORT)
    SnailJobRpcResult reportLogTask(List<LogTaskDTO> list);

    @Mapping(method = RequestMethod.POST, path = HTTP_PATH.SYNC_CONFIG)
    Result syncRemoteConfig();

    @Mapping(method = RequestMethod.POST, path = HTTP_PATH.BEAT)
    Result beat(String mark);

}
