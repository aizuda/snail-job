package com.aizuda.snailjob.server.common.alarm;

import com.aizuda.snailjob.common.core.enums.StatusEnum;
import com.aizuda.snailjob.server.common.AlarmInfoConverter;
import com.aizuda.snailjob.server.common.dto.JobAlarmInfo;
import com.aizuda.snailjob.server.common.dto.NotifyConfigInfo;
import com.aizuda.snailjob.server.common.enums.SystemModeEnum;
import com.aizuda.snailjob.server.common.triple.ImmutableTriple;
import com.aizuda.snailjob.server.common.triple.Triple;
import com.aizuda.snailjob.template.datasource.persistence.mapper.JobNotifyConfigMapper;
import com.aizuda.snailjob.template.datasource.persistence.po.JobNotifyConfig;
import com.aizuda.snailjob.server.common.triple.ImmutableTriple;
import com.aizuda.snailjob.server.common.triple.Triple;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.common.collect.Maps;
import org.springframework.beans.factory.annotation.Autowired;
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
public abstract class AbstractJobAlarm<E extends ApplicationEvent> extends AbstractAlarm<E, JobAlarmInfo, Long> {

    @Autowired
    private JobNotifyConfigMapper jobNotifyConfigMapper;

    @Override
    protected Map<Triple<String, String, Long>, List<JobAlarmInfo>> convertAlarmDTO(List<JobAlarmInfo> alarmInfos, Set<String> namespaceIds, Set<String> groupNames, Set<Long> jobIds) {

        return alarmInfos.stream().collect(Collectors.groupingBy(i -> {
            String namespaceId = i.getNamespaceId();
            String groupName = i.getGroupName();
            Long jobId = i.getJobId();
            namespaceIds.add(namespaceId);
            groupNames.add(groupName);
            jobIds.add(jobId);
            return ImmutableTriple.of(namespaceId, groupName, jobId);
        }));
    }

    @Override
    protected Map<Triple<String, String, Long>, List<NotifyConfigInfo>> obtainNotifyConfig(Set<String> namespaceIds, Set<String> groupNames, Set<Long> jobIds) {

        // 批量获取所需的通知配置
        List<JobNotifyConfig> jobNotifyConfigs = jobNotifyConfigMapper.selectList(
                new LambdaQueryWrapper<JobNotifyConfig>()
                        .eq(JobNotifyConfig::getNotifyStatus, StatusEnum.YES.getStatus())
                        .eq(JobNotifyConfig::getNotifyScene, getNotifyScene())
                        .in(JobNotifyConfig::getNamespaceId, namespaceIds)
                        .in(JobNotifyConfig::getGroupName, groupNames)
                        .in(JobNotifyConfig::getJobId, jobIds)
        );
        if (CollectionUtils.isEmpty(jobNotifyConfigs)) {
            return Maps.newHashMap();
        }

        List<NotifyConfigInfo> notifyConfigInfos = AlarmInfoConverter.INSTANCE.jobToNotifyConfigInfos(jobNotifyConfigs);

        return notifyConfigInfos.stream()
                .collect(Collectors.groupingBy(i -> {

                    String namespaceId = i.getNamespaceId();
                    String groupName = i.getGroupName();
                    Long jobId = i.getJobId();

                    return ImmutableTriple.of(namespaceId, groupName, jobId);
                }));

    }

    @Override
    protected String rateLimiterKey(NotifyConfigInfo notifyConfig) {
        return MessageFormat.format("{}_{}", SystemModeEnum.JOB.name(), notifyConfig.getId());
    }
}
