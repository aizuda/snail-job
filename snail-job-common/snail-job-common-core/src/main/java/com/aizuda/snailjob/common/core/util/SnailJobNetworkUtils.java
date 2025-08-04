package com.aizuda.snailjob.common.core.util;

import cn.hutool.core.collection.CollUtil;
import com.aizuda.snailjob.common.core.network.SnailJobNetworkProperties;
import com.aizuda.snailjob.common.log.SnailJobLog;

import java.io.IOException;
import java.net.*;
import java.util.Enumeration;
import java.util.List;
import java.util.regex.Pattern;

public class SnailJobNetworkUtils {
    private static final Pattern IPV4_PATTERN = Pattern.compile(
            "^(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])$");

    private final SnailJobNetworkProperties properties;

    public SnailJobNetworkUtils(SnailJobNetworkProperties properties) {
        this.properties = properties;
    }

    /**
     * 获取符合条件的首选IP地址
     */
    public String findPreferredHostAddress() {
        InetAddress address = findFirstNonLoopbackAddress();
        if (address != null) {
            return address.getHostAddress();
        }

        return NetUtil.getLocalIpStr();
    }


    public InetAddress findFirstNonLoopbackAddress() {
        InetAddress result = null;
        try {
            int lowest = Integer.MAX_VALUE;
            for (Enumeration<NetworkInterface> nics = NetworkInterface.getNetworkInterfaces(); nics.hasMoreElements(); ) {
                NetworkInterface ifc = nics.nextElement();
                if (ifc.isUp()) {
                    SnailJobLog.LOCAL.trace("Testing interface: " + ifc.getDisplayName());
                    if (ifc.getIndex() < lowest || result == null) {
                        lowest = ifc.getIndex();
                    } else if (result != null) {
                        continue;
                    }

                    if (!ignoreInterface(ifc.getDisplayName())) {
                        for (Enumeration<InetAddress> addrs = ifc.getInetAddresses(); addrs.hasMoreElements(); ) {
                            InetAddress address = addrs.nextElement();
                            if (isValidAddress(address) && isPreferredAddress(address)) {
                                SnailJobLog.LOCAL.trace("Found non-loopback interface: " + ifc.getDisplayName());
                                result = address;
                            }
                        }
                    }
                }
            }
        } catch (IOException ex) {
            SnailJobLog.LOCAL.error("Cannot get first non-loopback address", ex);
        }

        if (result != null) {
            return result;
        }

        return NetUtil.getLocalAddress();
    }

    private boolean isValidAddress(InetAddress address) {
        if (address.isLoopbackAddress()) {
            return false;
        }

        // 如果配置了优先IPv4，则排除IPv6地址
        if (properties.isPreferIpv4() && address instanceof Inet6Address) {
            return false;
        }

        // 如果配置了优先本地地址，则检查是否为本地地址
        if (properties.isPreferSiteLocalAddress() && !address.isSiteLocalAddress()) {
            return false;
        }

        return true;
    }

    // For testing.
    private boolean isPreferredAddress(InetAddress address) {

        final List<String> preferredNetworks = this.properties.getPreferredNetworks();
        if (CollUtil.isEmpty(preferredNetworks)) {
            return true;
        }

        for (String regex : preferredNetworks) {

            final String hostAddress = address.getHostAddress();
            if (hostAddress.matches(regex) || hostAddress.startsWith(regex) || matchesCIDR(address, regex)) {
                return true;
            }
        }
        SnailJobLog.LOCAL.info("Ignoring address: " + address.getHostAddress());
        return false;
    }


    // For testing
    boolean ignoreInterface(String interfaceName) {
        for (String regex : this.properties.getIgnoredInterfaces()) {
            if (interfaceName.matches(regex)) {
                SnailJobLog.LOCAL.trace("Ignoring interface: " + interfaceName);
                return true;
            }
        }
        return false;
    }

    private boolean matchesCIDR(InetAddress address, String regex) {
        String[] parts = regex.split("/");
        if (parts.length != 2) {
            return false;
        }

        String baseIp = parts[0];
        int prefixLength = Integer.parseInt(parts[1]);
        if (address instanceof Inet6Address && !IPV4_PATTERN.matcher(baseIp).matches()) {
            // IPv6匹配逻辑
            byte[] addrBytes = address.getAddress();
            byte[] baseBytes = textToNumericFormatV6(baseIp);

            if (baseBytes == null || addrBytes.length != baseBytes.length) {
                return false;
            }

            int bitsToCompare = Math.min(prefixLength, addrBytes.length * 8);
            for (int i = 0; i < bitsToCompare / 8; i++) {
                if (addrBytes[i] != baseBytes[i]) {
                    return false;
                }
            }

            int remainingBits = bitsToCompare % 8;
            if (remainingBits != 0) {
                int mask = 0xFF << (8 - remainingBits);
                if ((addrBytes[bitsToCompare / 8] & mask) != (baseBytes[bitsToCompare / 8] & mask)) {
                    return false;
                }
            }

            return true;
        } else if (address instanceof Inet4Address && IPV4_PATTERN.matcher(baseIp).matches()) {
            // IPv4匹配逻辑
            byte[] addrBytes = address.getAddress();
            String[] baseParts = baseIp.split("\\.");
            int[] baseOctets = new int[4];

            for (int i = 0; i < 4; i++) {
                baseOctets[i] = Integer.parseInt(baseParts[i]);
            }

            int maskBits = prefixLength;
            int mask = -1 << (32 - maskBits);

            int addrInt = ((addrBytes[0] & 0xFF) << 24) |
                    ((addrBytes[1] & 0xFF) << 16) |
                    ((addrBytes[2] & 0xFF) << 8) |
                    (addrBytes[3] & 0xFF);

            int baseInt = (baseOctets[0] << 24) |
                    (baseOctets[1] << 16) |
                    (baseOctets[2] << 8) |
                    baseOctets[3];

            return (addrInt & mask) == (baseInt & mask);
        }

        return false;
    }

    // IPv6文本格式转字节数组
    private byte[] textToNumericFormatV6(String ipAddress) {
        // 简化实现，实际应使用更完善的IPv6解析
        try {
            return Inet6Address.getByName(ipAddress).getAddress();
        } catch (UnknownHostException e) {
            return null;
        }
    }
}