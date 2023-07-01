package com.aizuda.easy.retry.server.dto;

import lombok.Data;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;

/**
 * 注册的节点信息
 *
 * @author: www.byteblogs.com
 * @date : 2023-06-09 11:02
 */
@Data
public class RegisterNodeInfo implements Comparable<RegisterNodeInfo> {

    private String groupName;

    private String hostId;

    private String hostIp;

    private Integer hostPort;

    private LocalDateTime expireAt;

    private Integer nodeType;

    private String contextPath;

    @Override
    public int compareTo(@NotNull RegisterNodeInfo info) {
        return hostId.compareTo(info.hostId);
    }
}
