package com.aizuda.snailjob.server.retry.task.dto;

import com.aizuda.snailjob.server.common.dto.PartitionTask;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Set;

/**
 * @author opensnail
 * @date 2025-01-11
 * @since 1.3.0-beta1.1
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class RetrySceneConfigPartitionTask extends PartitionTask {

    private String namespaceId;

    private String groupName;

    private String sceneName;

    /**
     * 通知告警场景配置id列表
     */
    private Set<Long> notifyIds;
}
