package com.aizuda.snailjob.server.common.dto;

import cn.hutool.core.util.StrUtil;
import com.aizuda.snailjob.common.core.util.JsonUtil;
import lombok.Data;

import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 注册的节点信息
 *
 * @author: opensnail
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

    private Map<String, String> labelMap;

    public String address() {
        return MessageFormat.format("{0}:{1}", hostIp, hostPort.toString());
    }

    public void setLabels(String labels) {
        if (StrUtil.isBlank(labels)) {
            return;
        }
        this.labelMap = JsonUtil.parseHashMap(labels);
    }

    @Override
    public int compareTo(RegisterNodeInfo info) {
        return hostId.compareTo(info.hostId);
    }
}
