package com.aizuda.snailjob.server.common.alarm;

import com.aizuda.snailjob.server.common.dto.JobAlarmInfo;
import com.aizuda.snailjob.server.common.dto.NotifyConfigInfo;
import com.aizuda.snailjob.server.common.enums.SystemModeEnum;
import com.aizuda.snailjob.server.common.triple.ImmutableTriple;
import com.aizuda.snailjob.server.common.triple.Triple;
import org.springframework.context.ApplicationEvent;

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
public abstract class AbstractJobAlarm<E extends ApplicationEvent> extends AbstractAlarm<E, JobAlarmInfo> {

    @Override
    protected Map<Triple<String, String, String>, List<JobAlarmInfo>> convertAlarmDTO(List<JobAlarmInfo> alarmInfos, Set<String> namespaceIds, Set<String> groupNames, Set<String> jobIds) {

        return alarmInfos.stream().collect(Collectors.groupingBy(i -> {
            String namespaceId = i.getNamespaceId();
            String groupName = i.getGroupName();
            String jobId = String.valueOf(i.getJobId());
            namespaceIds.add(namespaceId);
            groupNames.add(groupName);
            jobIds.add(jobId);
            return ImmutableTriple.of(namespaceId, groupName, jobId);
        }));
    }
}
