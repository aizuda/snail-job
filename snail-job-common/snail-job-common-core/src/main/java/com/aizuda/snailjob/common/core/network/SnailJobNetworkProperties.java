package com.aizuda.snailjob.common.core.network;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 *
 * </p>
 *
 * @author opensnail
 * @date 2025-07-31
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "snail-job.network")
public class SnailJobNetworkProperties {
    /**
     * 优先使用的网络段列表，支持CIDR表示法，如：192.168.1.0/24
     */
    private List<String> preferredNetworks;

    /**
     * 是否优先使用IPv4地址
     */
    private boolean preferIpv4 = true;

    /**
     * 是否优先私有地址
     * 如：
     * 10.0.0.0/8
     * 172.16.0.0/12
     * 192.168.0.0/16
     */
    private boolean preferSiteLocalAddress = true;

    /**
     * 忽略网卡信息
     */
    private List<String> ignoredInterfaces = new ArrayList<>();


}
