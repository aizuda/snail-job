package com.aizuda.snailjob.client.core.plugin;

import com.aizuda.snailjob.client.core.exception.SnailJobClientException;
import com.aizuda.snailjob.client.core.intercepter.RetrySiteSnapshot;
import com.aizuda.snailjob.common.core.constant.SystemConstants;
import com.aizuda.snailjob.common.log.SnailJobLog;
import com.aizuda.snailjob.common.core.model.SnailJobHeaders;
import com.aizuda.snailjob.common.core.util.JsonUtil;
import com.aizuda.snailjob.client.core.exception.SnailJobClientException;
import com.aizuda.snailjob.client.core.intercepter.RetrySiteSnapshot;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author: opensnail
 * @date : 2022-05-17 09:01
 */
@Slf4j
public class RequestHeaderPlugins {

    private RequestHeaderPlugins() {
    }

    /**
     * 请求头传递
     *
     * @return 头信息
     */
    public static Map<String, String> requestHeader() {

        Map<String, String> header = new HashMap<>();
        SnailJobHeaders retryHeader = RetrySiteSnapshot.getRetryHeader();

        // 传递请求头
        if (Objects.nonNull(retryHeader)) {
            long callRemoteTime = System.currentTimeMillis();
            Long entryMethodTime = RetrySiteSnapshot.getEntryMethodTime();
            if (Objects.isNull(entryMethodTime)) {
               SnailJobLog.LOCAL.warn("entry method time is null. retryId:[{}]", retryHeader.getRetryId());
            } else {
                long transmitTime = retryHeader.getDdl() - (callRemoteTime - entryMethodTime);
               SnailJobLog.LOCAL.info("RPC传递header头 callRemoteTime:[{}] - entryMethodTime:[{}] = transmitTime:[{}]", callRemoteTime, entryMethodTime, transmitTime);
                if (transmitTime > 0) {
                    retryHeader.setDdl(transmitTime);
                    // 重新刷新进入时间
                    RetrySiteSnapshot.setEntryMethodTime(System.currentTimeMillis());
                } else {
                    throw new SnailJobClientException("调用链超时, 不在继续调用后面请求");
                }
            }

            header.put(SystemConstants.SNAIL_JOB_HEAD_KEY, JsonUtil.toJsonString(retryHeader));
        }

        return header;
    }

}
