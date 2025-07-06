package com.aizuda.snailjob.server.common.dto;

import io.grpc.ManagedChannel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

/**
 * <p>
 * 活跃的实例信息
 * </p>
 *
 * @author opensnail
 * @date 2025-05-24
 */
@Setter
@Getter
@EqualsAndHashCode
public class InstanceLiveInfo implements Comparable<InstanceLiveInfo> {
    private long lastUpdateAt;
    private RegisterNodeInfo nodeInfo;
    private boolean alive;
    private ManagedChannel channel;

    @Override
    public int compareTo(InstanceLiveInfo o) {
        if (Objects.isNull(nodeInfo) || Objects.isNull(o) || Objects.isNull(o.getNodeInfo())) {
            return 0;
        }
        return nodeInfo.compareTo(o.getNodeInfo());
    }
}
