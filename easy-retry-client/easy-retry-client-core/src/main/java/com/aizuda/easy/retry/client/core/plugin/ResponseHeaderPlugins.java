package com.aizuda.easy.retry.client.core.plugin;

import com.aizuda.easy.retry.client.core.intercepter.RetrySiteSnapshot;
import com.aizuda.easy.retry.common.core.constant.SystemConstants;

import java.util.List;
import java.util.Map;

/**
 * @author: opensnail
 * @date : 2022-05-17 09:01
 */
public class ResponseHeaderPlugins {

    private ResponseHeaderPlugins() {
    }

    /**
     * 获取接口返回的响应头
     *
     * @param header 响应头
     */
    public static void responseHeader(Map<String, List<String>> header) {

        // 获取不重试标志
        if (header.containsKey(SystemConstants.EASY_RETRY_STATUS_CODE_KEY)) {
            List<String> statusCode = header.get(SystemConstants.EASY_RETRY_STATUS_CODE_KEY);
            RetrySiteSnapshot.setRetryStatusCode(statusCode.get(0));
        }
    }

}
