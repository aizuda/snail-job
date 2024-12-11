package com.aizuda.snailjob.server.job.task.server;

import com.aizuda.snailjob.common.core.model.Result;
import com.aizuda.snailjob.server.common.rpc.client.RequestMethod;
import com.aizuda.snailjob.server.common.rpc.client.annotation.Mapping;


import static com.aizuda.snailjob.common.core.constant.SystemConstants.HTTP_PATH.GET_REG_NODES_AND_REFRESH;

/**
 * @Author：srzou
 * @Package：com.aizuda.snailjob.server.job.task.server
 * @Project：snail-job
 * @Date：2024/12/11 9:36
 * @Filename：ServerRpcClient
 */
public interface ServerRpcClient {
    @Mapping(path = GET_REG_NODES_AND_REFRESH, method = RequestMethod.POST)
    Result<String> getRegNodesAndFlush();
}
