package com.aizuda.snailjob.server.common.alarm;

import cn.hutool.core.util.StrUtil;
import com.aizuda.snailjob.common.core.util.JsonUtil;
import com.aizuda.snailjob.common.core.util.StreamUtils;
import com.aizuda.snailjob.server.common.dto.WorkflowAlarmInfo;
import com.aizuda.snailjob.server.common.triple.ImmutableTriple;
import com.aizuda.snailjob.server.common.triple.Triple;
import org.springframework.context.ApplicationEvent;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author xiaowoniu
 * @date 2023-12-03 10:19:19
 * @since 2.5.0
 */
public abstract class AbstractWorkflowAlarm<E extends ApplicationEvent> extends AbstractAlarm<E, WorkflowAlarmInfo> {

    @Override
    protected Map<Triple<String, String, Set<Long>>, List<WorkflowAlarmInfo>> convertAlarmDTO(List<WorkflowAlarmInfo> workflowAlarmInfoList, Set<String> namespaceIds, Set<String> groupNames, Set<Long> notifyIds) {

        return StreamUtils.groupByKey(workflowAlarmInfoList, workflowAlarmInfo -> {
            String namespaceId = workflowAlarmInfo.getNamespaceId();
            String groupName = workflowAlarmInfo.getGroupName();
            HashSet<Long> notifyIdsSet = StrUtil.isBlank(workflowAlarmInfo.getNotifyIds()) ? new HashSet<>() : new HashSet<>(JsonUtil.parseList(workflowAlarmInfo.getNotifyIds(), Long.class));

            namespaceIds.add(namespaceId);
            groupNames.add(groupName);
            notifyIds.addAll(notifyIdsSet);
            return ImmutableTriple.of(namespaceId, groupName, notifyIdsSet);
        });
    }
}
