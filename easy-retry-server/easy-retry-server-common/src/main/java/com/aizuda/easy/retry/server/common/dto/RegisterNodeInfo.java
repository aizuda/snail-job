package com.aizuda.easy.retry.server.common.dto;

import lombok.Data;

import java.text.MessageFormat;
import java.time.LocalDateTime;

/**
 * 注册的节点信息
 *
 * @author: www.byteblogs.com
 * @date : 2023-06-09 11:02
 */
@Data
public class RegisterNodeInfo implements Comparable<RegisterNodeInfo> {

    private String namespaceId;

    private String groupName;

    private String hostId;

    private String hostIp;

    private Integer hostPort;

    private LocalDateTime expireAt;

    private Integer nodeType;

    private String contextPath;

    public String address() {
        return MessageFormat.format("{0}:{1}", hostIp, hostPort.toString());
    }

    @Override
    public int compareTo(RegisterNodeInfo info) {
        return hostId.compareTo(info.hostId);
    }
}
