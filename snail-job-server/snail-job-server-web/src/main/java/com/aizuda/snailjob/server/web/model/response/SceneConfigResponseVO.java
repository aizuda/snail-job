package com.aizuda.snailjob.server.web.model.response;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * @author: opensnail
 * @date : 2022-03-03 10:56
 */
@Data
public class SceneConfigResponseVO {

    private Long id;

    private String groupName;

    private String sceneName;

    private Integer sceneStatus;

    private Integer maxRetryCount;

    private Integer backOff;

    private String triggerInterval;

    private String description;

    private Long deadlineRequest;

    private Integer routeKey;

    private Integer blockStrategy;

    private Integer executorTimeout;

    private LocalDateTime createDt;

    private LocalDateTime updateDt;

    /**
     * 通知告警场景配置id列表
     */
    private Set<Long> notifyIds;

    /**
     * 回调状态 0、不开启 1、开启
     */
    private Integer cbStatus;

    /**
     * 回调触发类型
     */
    private Integer cbTriggerType;

    /**
     * 回调的最大执行次数
     */
    private int cbMaxCount;

    /**
     * 回调间隔时间
     */
    private String cbTriggerInterval;
}
