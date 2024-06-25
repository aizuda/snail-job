package com.aizuda.snailjob.common.core.util;

import com.aizuda.snailjob.common.log.SnailJobLog;

import java.io.IOException;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.text.MessageFormat;
import java.util.Enumeration;
import java.util.Optional;
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

    private static final Pattern IP_PATTERN = Pattern.compile("\\d{1,3}(\\.\\d{1,3}){3,5}$");
    private static final String ANY_HOST_VALUE = "0.0.0.0";
    private final static String LOCAL_HOST = "127.0.0.1";
    private final static String URL_IPV4 = "http://{0}:{1}/{2}";
    private final static String URL_IPV6 = "http://[{0}]:{1}/{2}";
    private static volatile InetAddress LOCAL_ADDRESS = null;

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
        return getLocalAddress().getHostAddress();
    }

    /**
     * 获取InetAddress借鉴 dubbo
     * see: org.apache.dubbo.common.utils.NetUtils#getLocalAddress
     *
     * @return InetAddress
     */
    public static InetAddress getLocalAddress() {
        if (LOCAL_ADDRESS != null) {
            return LOCAL_ADDRESS;
        }
        InetAddress localAddress = getLocalAddress0();
        LOCAL_ADDRESS = localAddress;
        return localAddress;
    }

    private static InetAddress getLocalAddress0() {
        InetAddress localAddress = null;
        try {
            localAddress = InetAddress.getLocalHost();
            Optional<InetAddress> addressOp = toValidAddress(localAddress);
            if (addressOp.isPresent()) {
                return addressOp.get();
            }
        } catch (Throwable e) {
            SnailJobLog.LOCAL.warn("get local address error", e);
        }

        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                try {
                    NetworkInterface network = interfaces.nextElement();
                    if (network.isLoopback() || network.isVirtual() || !network.isUp()) {
                        continue;
                    }
                    Enumeration<InetAddress> addresses = network.getInetAddresses();
                    while (addresses.hasMoreElements()) {
                        try {
                            Optional<InetAddress> addressOp = toValidAddress(addresses.nextElement());
                            if (addressOp.isPresent()) {
                                try {
                                    if (addressOp.get().isReachable(100)) {
                                        return addressOp.get();
                                    }
                                } catch (IOException e) {
                                    // ignore
                                }
                            }
                        } catch (Throwable e) {
                            SnailJobLog.LOCAL.warn("get local address error", e);
                        }
                    }
                } catch (Throwable e) {
                    SnailJobLog.LOCAL.warn("get local address error", e);
                }
            }
        } catch (Throwable e) {
            SnailJobLog.LOCAL.warn("get local address error", e);
        }

        return localAddress;
    }

    private static Optional<InetAddress> toValidAddress(InetAddress address) {
        if (address instanceof Inet6Address) {
            Inet6Address v6Address = (Inet6Address) address;
            if (isPreferIPV6Address()) {
                return Optional.ofNullable(normalizeV6Address(v6Address));
            }
        }
        if (isValidV4Address(address)) {
            return Optional.of(address);
        }
        return Optional.empty();
    }

    static boolean isValidV4Address(InetAddress address) {
        if (address == null || address.isLoopbackAddress()) {
            return false;
        }
        String name = address.getHostAddress();
        return (name != null
                && IP_PATTERN.matcher(name).matches()
                && !ANY_HOST_VALUE.equals(name)
                && !LOCAL_HOST.equals(name));
    }

    /**
     * normalize the ipv6 Address, convert scope name to scope id.
     * e.g.
     * convert
     * fe80:0:0:0:894:aeec:f37d:23e1%en0
     * to
     * fe80:0:0:0:894:aeec:f37d:23e1%5
     * <p>
     * The %5 after ipv6 address is called scope id.
     * see java doc of {@link Inet6Address} for more details.
     *
     * @param address the input address
     * @return the normalized address, with scope id converted to int
     */
    static InetAddress normalizeV6Address(Inet6Address address) {
        String addr = address.getHostAddress();
        int i = addr.lastIndexOf('%');
        if (i > 0) {
            try {
                return InetAddress.getByName(addr.substring(0, i) + '%' + address.getScopeId());
            } catch (UnknownHostException e) {
                // ignore
                SnailJobLog.LOCAL.debug("Unknown IPV6 address: ", e);
            }
        }
        return address;
    }

    /**
     * Check if an ipv6 address
     *
     * @return true if it is reachable
     */
    static boolean isPreferIPV6Address() {
        boolean preferIpv6 = Boolean.getBoolean("java.net.preferIPv6Addresses");
        if (!preferIpv6) {
            return false;
        }
        return false;
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
