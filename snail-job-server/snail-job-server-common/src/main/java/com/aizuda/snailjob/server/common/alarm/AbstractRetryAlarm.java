package com.aizuda.snailjob.server.common.alarm;

import cn.hutool.core.util.StrUtil;
import com.aizuda.snailjob.common.core.util.JsonUtil;
import com.aizuda.snailjob.common.core.util.StreamUtils;
import com.aizuda.snailjob.server.common.dto.RetryAlarmInfo;
import com.aizuda.snailjob.template.datasource.persistence.po.RetrySceneConfig;
import org.springframework.context.ApplicationEvent;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author xiaowoniu
 * @date 2023-12-03 10:19:19
 * @since 2.5.0
 */
public abstract class AbstractRetryAlarm<E extends ApplicationEvent> extends AbstractAlarm<E, RetryAlarmInfo> {

    @Override
    protected Map<Set<Long>, List<RetryAlarmInfo>> convertAlarmDTO(List<RetryAlarmInfo> retryAlarmInfoList, Set<Integer> notifyScene, Set<Long> notifyIds) {

        return StreamUtils.groupByKey(retryAlarmInfoList, retryAlarmInfo -> {

            // 重试任务查询场景告警通知
            RetrySceneConfig retrySceneConfig = accessTemplate.getSceneConfigAccess().getSceneConfigByGroupNameAndSceneName(
                    retryAlarmInfo.getGroupName(),
                    retryAlarmInfo.getSceneName(),
                    retryAlarmInfo.getNamespaceId());
            Set<Long> retryNotifyIds = StrUtil.isBlank(retrySceneConfig.getNotifyIds()) ? new HashSet<>() : new HashSet<>(JsonUtil.parseList(retrySceneConfig.getNotifyIds(), Long.class));

            notifyScene.add(retryAlarmInfo.getNotifyScene());
            notifyIds.addAll(retryNotifyIds);
            return retryNotifyIds;
        });
    }
}
