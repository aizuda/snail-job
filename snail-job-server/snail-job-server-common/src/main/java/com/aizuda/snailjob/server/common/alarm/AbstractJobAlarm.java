package com.aizuda.snailjob.server.common.alarm;

import com.aizuda.snailjob.client.common.annotation.AbstractAlarm;
import com.aizuda.snailjob.common.core.util.JsonUtil;
import com.aizuda.snailjob.common.core.util.StreamUtils;
import com.aizuda.snailjob.server.common.dto.JobAlarmInfo;
import com.aizuda.snailjob.server.common.triple.ImmutableTriple;
import com.aizuda.snailjob.server.common.triple.Triple;
import org.springframework.context.ApplicationEvent;

import java.util.*;

/**
 * @author xiaowoniu
 * @date 2023-12-03 10:19:19
 * @since 2.5.0
 */
public abstract class AbstractJobAlarm<E extends ApplicationEvent> extends AbstractAlarm<E, JobAlarmInfo> {

    @Override
    protected Map<Triple<String, String, Set<Long>>, List<JobAlarmInfo>> convertAlarmDTO(List<JobAlarmInfo> alarmInfos, Set<String> namespaceIds, Set<String> groupNames, Set<Long> notifyIds) {

        return StreamUtils.groupByKey(alarmInfos, alarmInfo -> {
            String namespaceId = alarmInfo.getNamespaceId();
            String groupName = alarmInfo.getGroupName();
            HashSet<Long> notifyIdsSet = Objects.isNull(alarmInfo.getNotifyIds()) ? new HashSet<>() : new HashSet<>(JsonUtil.parseList(alarmInfo.getNotifyIds(), Long.class));

            namespaceIds.add(namespaceId);
            groupNames.add(groupName);
            notifyIds.addAll(notifyIdsSet);
            return ImmutableTriple.of(namespaceId, groupName, notifyIdsSet);
        });
    }
}
