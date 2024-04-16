package com.aizuda.snailjob.client.core.plugin;

import com.aizuda.snailjob.client.core.intercepter.RetrySiteSnapshot;
import com.aizuda.snailjob.common.core.constant.SystemConstants;
import com.aizuda.snailjob.client.core.intercepter.RetrySiteSnapshot;

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
        if (header.containsKey(SystemConstants.SNAIL_JOB_STATUS_CODE_KEY)) {
            List<String> statusCode = header.get(SystemConstants.SNAIL_JOB_STATUS_CODE_KEY);
            RetrySiteSnapshot.setRetryStatusCode(statusCode.get(0));
        }
    }

}
