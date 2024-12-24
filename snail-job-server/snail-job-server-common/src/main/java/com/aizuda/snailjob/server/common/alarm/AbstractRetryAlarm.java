package com.aizuda.snailjob.server.common.alarm;

import cn.hutool.core.util.StrUtil;
import com.aizuda.snailjob.common.core.util.JsonUtil;
import com.aizuda.snailjob.server.common.dto.RetryAlarmInfo;
import com.aizuda.snailjob.server.common.triple.ImmutableTriple;
import com.aizuda.snailjob.template.datasource.persistence.po.RetrySceneConfig;
import com.google.common.collect.Lists;
import org.springframework.context.ApplicationEvent;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author xiaowoniu
 * @date 2023-12-03 10:19:19
 * @since 2.5.0
 */
public abstract class AbstractRetryAlarm<E extends ApplicationEvent> extends AbstractAlarm<E, RetryAlarmInfo> {

    @Override
    protected Map<Long, List<RetryAlarmInfo>> convertAlarmDTO(List<RetryAlarmInfo> retryAlarmInfoList, Set<Integer> notifyScene) {

        Map<Long, List<RetryAlarmInfo>> retryAlarmInfoMap = new HashMap<>();
        // 重试任务查询场景告警通知
        Set<String> groupNames = new HashSet<>(), sceneNames = new HashSet<>(), namespaceIds = new HashSet<>();
        for (RetryAlarmInfo retryAlarmInfo : retryAlarmInfoList) {
            groupNames.add(retryAlarmInfo.getGroupName());
            sceneNames.add(retryAlarmInfo.getSceneName());
            namespaceIds.add(retryAlarmInfo.getNamespaceId());
            notifyScene.add(retryAlarmInfo.getNotifyScene());
        }

        // 按组名、场景名、命名空间分组
        Map<ImmutableTriple<String, String, String>, RetrySceneConfig> retrySceneConfigMap = accessTemplate.getSceneConfigAccess()
                .getSceneConfigByGroupNameAndSceneNameList(
                groupNames, sceneNames, namespaceIds)
                .stream().collect(Collectors.toMap(i -> ImmutableTriple.of(
                        i.getGroupName(),
                        i.getSceneName(),
                        i.getNamespaceId()),
                Function.identity()
        ));

        for (RetryAlarmInfo retryAlarmInfo : retryAlarmInfoList) {
            RetrySceneConfig retrySceneConfig = retrySceneConfigMap.get(ImmutableTriple.of(
                    retryAlarmInfo.getGroupName(),
                    retryAlarmInfo.getSceneName(),
                    retryAlarmInfo.getNamespaceId()));
            Set<Long> retryNotifyIds = StrUtil.isBlank(retrySceneConfig.getNotifyIds()) ? new HashSet<>() : new HashSet<>(JsonUtil.parseList(retrySceneConfig.getNotifyIds(), Long.class));

            for (Long retryNotifyId : retryNotifyIds) {
                List<RetryAlarmInfo> retryAlarmInfos = retryAlarmInfoMap.getOrDefault(retryNotifyId, Lists.newArrayList());
                retryAlarmInfos.add(retryAlarmInfo);
                retryAlarmInfoMap.put(retryNotifyId, retryAlarmInfos);
            }
        }

        return retryAlarmInfoMap;
    }
}
