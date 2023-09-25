package com.aizuda.easy.retry.server.common.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author www.byteblogs.com
 * @date 2023-09-25 22:48:36
 * @since 2.4.0
 */
@Data
public class FilterStrategyContext {

    private Long id;

    private RegisterNodeInfo registerNodeInfo;

    private String groupName;

    private LocalDateTime nextTriggerAt;


    private String uniqueId;

    private List<String> sceneBlacklist;

    private String sceneName;


}
