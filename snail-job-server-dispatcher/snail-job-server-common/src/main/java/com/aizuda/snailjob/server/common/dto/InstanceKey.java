package com.aizuda.snailjob.server.common.dto;

import cn.hutool.core.builder.CompareToBuilder;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

/**
 * <p>
 *
 * </p>
 *
 * @author opensnail
 * @date 2025-05-24
 */
@Getter
@Setter
@Builder
public class InstanceKey implements Comparable<InstanceKey> {
    private String groupName;
    private String namespaceId;
    private String hostId;

    @Override
    public int compareTo(final InstanceKey other) {
        return new CompareToBuilder()
                .append(other.getNamespaceId(), getNamespaceId())
                .append(other.getGroupName(), getGroupName())
                .append(other.getHostId(), getHostId())
                .toComparison();
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof InstanceKey) {
            final InstanceKey other = (InstanceKey) obj;
            return Objects.equals(getNamespaceId(), other.getNamespaceId())
                    && Objects.equals(getGroupName(), other.getGroupName())
                    && Objects.equals(getHostId(), other.getHostId())
                    ;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return (getNamespaceId() == null ? 0 : getNamespaceId().hashCode()) ^
                (getGroupName() == null ? 0 : getGroupName().hashCode()) ^
                (getHostId() == null ? 0 : getHostId().hashCode());
    }

    @Override
    public String toString() {
        return "(" + getNamespaceId() + ',' + getGroupName() + ',' + getHostId() + ')';
    }

}
