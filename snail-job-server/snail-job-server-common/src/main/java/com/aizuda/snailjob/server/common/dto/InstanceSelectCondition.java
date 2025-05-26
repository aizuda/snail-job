package com.aizuda.snailjob.server.common.dto;

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
    private Map<String, String> targetLabel;
}
