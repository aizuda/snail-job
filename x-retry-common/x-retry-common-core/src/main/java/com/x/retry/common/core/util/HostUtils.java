package com.x.retry.common.core.util;

import com.x.retry.common.core.exception.XRetryCommonException;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * @author: www.byteblogs.com
 * @date : 2021-11-25 17:36
 */
public class HostUtils {


    public static String getIp() {

        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            throw new XRetryCommonException("未获取HostAddress");
        }

    }
}
