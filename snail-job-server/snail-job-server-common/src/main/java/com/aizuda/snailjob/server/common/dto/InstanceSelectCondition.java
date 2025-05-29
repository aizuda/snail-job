package com.aizuda.snailjob.server.common.dto;

import cn.hutool.core.util.StrUtil;
import com.aizuda.snailjob.common.core.util.JsonUtil;
import com.google.common.collect.Maps;
import lombok.Builder;
import lombok.Getter;

import java.util.Map;

/**
 * <p>
 *
 * </p>
 *
 * @author opensnail
 * @date 2025-05-24
 */
@Builder
@Getter
public class InstanceSelectCondition {
    private String allocKey;
    private String groupName;
    private String namespaceId;
    private Integer routeKey;
    private String targetLabels;

    public Map<String, String> getTargetLabels() {
        return StrUtil.isNotBlank(targetLabels) ? JsonUtil.parseHashMap(targetLabels) : Maps.newHashMap();
    }
}
