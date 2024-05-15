package com.aizuda.snailjob.common.core.util;

import com.aizuda.snailjob.common.core.exception.SnailJobCommonException;
import com.aizuda.snailjob.common.core.exception.SnailJobCommonException;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.MessageFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Snail Job 网络相关工具
 *
 * @author xiaowoniu
 * @date 2024-03-06 22:19:34
 * @since 3.1.0
 */
public class NetUtil {

    private final static String LOCAL_HOST = "127.0.0.1";
    private final static String URL_IPV4 = "http://{0}:{1}/{2}";
    private final static String URL_IPV6 = "http://[{0}]:{1}/{2}";

    /**
     * 获取URL
     *
     * @param host        主机地址
     * @param port        端口
     * @param contextPath 统一后缀
     * @return URL
     */
    public static String getUrl(String host, int port, String contextPath) {

        String url = URL_IPV4;
        if (isValidIpV6(host)) {
            url = URL_IPV6;
        }

        return MessageFormat.format(url, host, String.valueOf(port), contextPath);
    }

    /**
     * 获取本地IP
     *
     * @return 本机网卡IP地址
     */
    public static String getLocalIpStr() {

        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            throw new SnailJobCommonException("未获取HostAddress");
        }

    }

    /**
     * ipv4校验是否是ipv4
     *
     * @param host 主机地址
     * @return
     */
    private static boolean isValidIpV4(String host) {
        Pattern pattern = Pattern.compile("^(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])$");
        Matcher matcher = pattern.matcher(host);
        return matcher.matches();
    }

    /**
     * ipv4校验是否是ipv6
     *
     * @param host 主机地址
     * @return
     */
    private static boolean isValidIpV6(String host) {
        Pattern pattern = Pattern.compile("^([0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}$");
        Matcher matcher = pattern.matcher(host);
        return matcher.matches();
    }

}
