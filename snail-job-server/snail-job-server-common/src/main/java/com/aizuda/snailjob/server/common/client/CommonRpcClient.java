package com.aizuda.snailjob.server.common.client;

import com.aizuda.snailjob.common.core.model.Result;
import com.aizuda.snailjob.server.common.rpc.client.RequestMethod;
import com.aizuda.snailjob.server.common.rpc.client.annotation.Body;
import com.aizuda.snailjob.server.common.rpc.client.annotation.Mapping;
import com.aizuda.snailjob.server.model.dto.ConfigDTO;

import static com.aizuda.snailjob.common.core.constant.SystemConstants.HTTP_PATH.GET_REG_NODES_AND_REFRESH;
import static com.aizuda.snailjob.common.core.constant.SystemConstants.HTTP_PATH.SYNC_CONFIG;

/**
 * 调用客户端接口
 *
 * @author: opensnail
 * @date : 2023-06-19 15:40
 * @since sj_1.0.0
 */
public interface CommonRpcClient {

    @Mapping(path = SYNC_CONFIG, method = RequestMethod.POST)
    Result syncConfig(@Body ConfigDTO configDTO);

    @Mapping(path = GET_REG_NODES_AND_REFRESH, method = RequestMethod.POST)
    Result<String> getRegNodesAndFlush();
}
