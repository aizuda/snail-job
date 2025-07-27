package com.aizuda.snailjob.client.common.client;

import com.aizuda.snailjob.client.common.annotation.Mapping;
import com.aizuda.snailjob.client.common.annotation.SnailEndPoint;
import com.aizuda.snailjob.client.common.cache.GroupVersionCache;
import com.aizuda.snailjob.client.common.rpc.client.RequestMethod;
import com.aizuda.snailjob.model.request.ConfigRequest;
import com.aizuda.snailjob.common.core.model.Result;

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
    public Result syncVersion(ConfigRequest configRequest) {
        GroupVersionCache.setConfig(configRequest);
        return new Result();
    }

}
