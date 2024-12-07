package com.aizuda.snailjob.server.common.alarm;

import com.aizuda.snailjob.client.common.annotation.AbstractAlarm;
import com.aizuda.snailjob.common.core.util.JsonUtil;
import com.aizuda.snailjob.common.core.util.StreamUtils;
import com.aizuda.snailjob.server.common.dto.RetryAlarmInfo;
import com.aizuda.snailjob.server.common.triple.ImmutableTriple;
import com.aizuda.snailjob.server.common.triple.Triple;
import org.springframework.context.ApplicationEvent;

import java.util.*;

/**
 * @author xiaowoniu
 * @date 2023-12-03 10:19:19
 * @since 2.5.0
 */
public abstract class AbstractRetryAlarm<E extends ApplicationEvent> extends AbstractAlarm<E, RetryAlarmInfo> {
    @Override
    protected Map<Triple<String, String, Set<Long>>, List<RetryAlarmInfo>> convertAlarmDTO(
            List<RetryAlarmInfo> retryAlarmInfoList,
            Set<String> namespaceIds,
            Set<String> groupNames,
            Set<Long> notifyIds) {

        return StreamUtils.groupByKey(retryAlarmInfoList, retryAlarmInfo -> {
            String namespaceId = retryAlarmInfo.getNamespaceId();
            String groupName = retryAlarmInfo.getGroupName();
            HashSet<Long> notifyIdsSet = Objects.isNull(retryAlarmInfo.getNotifyIds()) ? new HashSet<>() : new HashSet<>(JsonUtil.parseList(retryAlarmInfo.getNotifyIds(), Long.class));

            namespaceIds.add(namespaceId);
            groupNames.add(groupName);
            notifyIds.addAll(notifyIdsSet);
            return ImmutableTriple.of(namespaceId, groupName, notifyIdsSet);
        });
    }
}
