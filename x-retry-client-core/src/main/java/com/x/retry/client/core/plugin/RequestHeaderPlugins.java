package com.x.retry.client.core.plugin;

import com.x.retry.client.core.exception.XRetryClientException;
import com.x.retry.client.core.intercepter.RetrySiteSnapshot;
import com.x.retry.common.core.constant.SystemConstants;
import com.x.retry.common.core.log.LogUtils;
import com.x.retry.common.core.model.XRetryHeaders;
import com.x.retry.common.core.util.JsonUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author: www.byteblogs.com
 * @date : 2022-05-17 09:01
 */
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
        XRetryHeaders retryHeader = RetrySiteSnapshot.getRetryHeader();

        // 传递请求头
        if (Objects.nonNull(retryHeader)) {
            long callRemoteTime = System.currentTimeMillis();
            long entryMethodTime = RetrySiteSnapshot.getEntryMethodTime();
            long transmitTime = retryHeader.getDdl() - (callRemoteTime - entryMethodTime);
            LogUtils.info("RPC传递header头 entryMethodTime:[{}] - callRemoteTime:[{}] = transmitTime:[{}]", entryMethodTime, callRemoteTime, transmitTime);
            if (transmitTime > 0) {
                retryHeader.setDdl(transmitTime);
            } else {
                throw new XRetryClientException("调用链超时, 不在继续调用后面请求");
            }

            header.put(SystemConstants.X_RETRY_HEAD_KEY, JsonUtil.toJsonString(retryHeader));
        }

        return header;
    }

}
