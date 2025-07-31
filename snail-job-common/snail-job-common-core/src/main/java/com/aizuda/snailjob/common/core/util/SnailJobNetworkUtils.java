package com.aizuda.snailjob.common.core.util;

import com.aizuda.snailjob.common.core.network.SnailJobNetworkProperties;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
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
        List<InetAddress> validAddresses = findAllValidAddresses();
        return selectPreferredAddress(validAddresses);
    }

    private List<InetAddress> findAllValidAddresses() {
        List<InetAddress> result = new ArrayList<>();
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface ni = interfaces.nextElement();
                if (ni.isUp()) {
                    Enumeration<InetAddress> addresses = ni.getInetAddresses();
                    while (addresses.hasMoreElements()) {
                        InetAddress address = addresses.nextElement();
                        if (isValidAddress(address)) {
                            result.add(address);
                        }
                    }
                }
            }
        } catch (SocketException e) {
            throw new RuntimeException("Failed to retrieve network interfaces", e);
        }
        return result;
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

    private String selectPreferredAddress(List<InetAddress> addresses) {
        // 首先尝试按优先网络段匹配
        if (properties.getPreferredNetworks() != null && !properties.getPreferredNetworks().isEmpty()) {
            for (String network : properties.getPreferredNetworks()) {
                String[] parts = network.split("/");
                String baseIp = parts[0];
                int prefixLength = Integer.parseInt(parts[1]);

                for (InetAddress address : addresses) {
                    if (matchesNetwork(address, baseIp, prefixLength)) {
                        return address.getHostAddress();
                    }
                }
            }
        }

        // 如果没有匹配的网络段，返回第一个有效的地址
        if (!addresses.isEmpty()) {
            return addresses.get(0).getHostAddress();
        }

        // 最后尝试使用回环地址
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            return "127.0.0.1";
        }
    }

    private boolean matchesNetwork(InetAddress address, String baseIp, int prefixLength) {
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