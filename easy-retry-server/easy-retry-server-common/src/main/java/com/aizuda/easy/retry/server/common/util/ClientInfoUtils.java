package com.aizuda.easy.retry.server.common.util;

import cn.hutool.core.util.StrUtil;
import com.aizuda.easy.retry.server.common.dto.RegisterNodeInfo;
import com.google.common.base.Splitter;

import java.util.List;

/**
 * @author: www.byteblogs.com
 * @date : 2023-10-16 15:20
 * @since : 2.4.0
 */
public class ClientInfoUtils {

    public static String generate(RegisterNodeInfo registerNodeInfo) {
        return registerNodeInfo.getHostId() + StrUtil.AT + registerNodeInfo.address();
    }

    public static String clientId(String clientInfo) {
        return split(clientInfo).get(0);
    }

    public static String address(String clientInfo) {
        return split(clientInfo).get(1);
    }

    public static List<String> split(String clientInfo) {
        return Splitter.on(StrUtil.AT).trimResults().splitToList(clientInfo);
    }

}
