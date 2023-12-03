package com.aizuda.easy.retry.server.common.alarm;

import com.aizuda.easy.retry.common.core.enums.NotifySceneEnum;
import com.aizuda.easy.retry.common.core.enums.StatusEnum;
import com.aizuda.easy.retry.server.common.AlarmInfoConverter;
import com.aizuda.easy.retry.server.common.dto.NotifyConfigInfo;
import com.aizuda.easy.retry.server.common.dto.RetryAlarmInfo;
import com.aizuda.easy.retry.server.common.enums.SystemModeEnum;
import com.aizuda.easy.retry.server.common.triple.ImmutableTriple;
import com.aizuda.easy.retry.server.common.triple.Triple;
import com.aizuda.easy.retry.template.datasource.persistence.po.NotifyConfig;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.common.collect.Maps;
import org.springframework.context.ApplicationEvent;
import org.springframework.util.CollectionUtils;

import java.text.MessageFormat;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author xiaowoniu
 * @date 2023-12-03 10:19:19
 * @since 2.5.0
 */
public abstract class AbstractRetryAlarm<E extends ApplicationEvent> extends AbstractAlarm<E, RetryAlarmInfo, String> {
    @Override
    protected Map<Triple<String, String, String>, List<RetryAlarmInfo>> convertAlarmDTO(
            List<RetryAlarmInfo> alarmDataList,
            Set<String> namespaceIds,
            Set<String> groupNames,
            Set<String> sceneNames) {

        return alarmDataList.stream()
                .collect(Collectors.groupingBy(i -> {

                    String namespaceId = i.getNamespaceId();
                    String groupName = i.getGroupName();
                    String sceneName = i.getSceneName();

                    namespaceIds.add(namespaceId);
                    groupNames.add(groupName);
                    sceneNames.add(sceneName);

                    return ImmutableTriple.of(namespaceId, groupName, sceneName);
                }));
    }


    @Override
    protected Map<Triple<String, String, String>, List<NotifyConfigInfo>> obtainNotifyConfig(Set<String> namespaceIds, Set<String> groupNames, Set<String> sceneNames) {

        // 批量获取所需的通知配置
        List<NotifyConfig> notifyConfigs = accessTemplate.getNotifyConfigAccess().list(
                new LambdaQueryWrapper<NotifyConfig>()
                        .eq(NotifyConfig::getNotifyStatus, StatusEnum.YES)
                        .eq(NotifyConfig::getNotifyScene, getNotifyScene())
                        .in(NotifyConfig::getNamespaceId, namespaceIds)
                        .in(NotifyConfig::getGroupName, groupNames)
                        .in(NotifyConfig::getSceneName, sceneNames)
        );

        if (CollectionUtils.isEmpty(notifyConfigs)) {
            return Maps.newHashMap();
        }

        List<NotifyConfigInfo> notifyConfigInfos = AlarmInfoConverter.INSTANCE.retryToNotifyConfigInfos(notifyConfigs);
        return notifyConfigInfos.stream()
                .collect(Collectors.groupingBy(config -> {

                    String namespaceId = config.getNamespaceId();
                    String groupName = config.getGroupName();
                    String sceneName = config.getSceneName();

                    return ImmutableTriple.of(namespaceId, groupName, sceneName);
                }));
    }

    @Override
    protected String rateLimiterKey(NotifyConfigInfo notifyConfig) {
        return MessageFormat.format("{}_{}", SystemModeEnum.RETRY.name(), notifyConfig.getId());
    }

}
