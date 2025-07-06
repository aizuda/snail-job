package com.aizuda.snailjob.server.common.client;

import com.aizuda.snailjob.common.core.model.Result;
import com.aizuda.snailjob.server.common.dto.PullRemoteNodeClientRegisterInfoDTO;
import com.aizuda.snailjob.server.common.dto.UpdateClientInfoDTO;
import com.aizuda.snailjob.server.common.rpc.client.RequestMethod;
import com.aizuda.snailjob.server.common.rpc.client.annotation.Body;
import com.aizuda.snailjob.server.common.rpc.client.annotation.Mapping;
import com.aizuda.snailjob.server.model.dto.ConfigDTO;
import com.aizuda.snailjob.template.datasource.persistence.po.ServerNode;

import static com.aizuda.snailjob.common.core.constant.SystemConstants.HTTP_PATH.*;

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
    Result<String> pullRemoteNodeClientRegisterInfo(@Body PullRemoteNodeClientRegisterInfoDTO registerInfo);

    @Mapping(path = UPDATE_CLIENT_INFO, method = RequestMethod.POST)
    Result<Boolean> updateClientInfo(@Body UpdateClientInfoDTO clientInfoDTO);
}
