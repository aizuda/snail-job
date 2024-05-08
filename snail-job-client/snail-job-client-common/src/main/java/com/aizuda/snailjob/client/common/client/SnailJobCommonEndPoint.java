package com.aizuda.snailjob.client.common.client;

import com.aizuda.snailjob.client.common.annotation.Mapping;
import com.aizuda.snailjob.client.common.annotation.SnailEndPoint;
import com.aizuda.snailjob.client.common.cache.GroupVersionCache;
import com.aizuda.snailjob.client.common.rpc.client.RequestMethod;
import com.aizuda.snailjob.common.core.model.Result;
import com.aizuda.snailjob.server.model.dto.ConfigDTO;

import static com.aizuda.snailjob.common.core.constant.SystemConstants.HTTP_PATH.SYNC_CONFIG;

/**
 * SnailJob 通用EndPoint
 *
 * @author: opensnail
 * @date : 2022-03-09 16:33
 */
@SnailEndPoint
public class SnailJobCommonEndPoint {

    /**
     * 同步版本
     */
    @Mapping(path = SYNC_CONFIG, method = RequestMethod.POST)
    public Result syncVersion(ConfigDTO configDTO) {
        GroupVersionCache.setConfig(configDTO);
        return new Result();
    }

}
