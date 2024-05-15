package com.aizuda.snailjob.server.common;

import cn.hutool.core.util.StrUtil;
import com.aizuda.snailjob.common.core.util.JsonUtil;
import com.aizuda.snailjob.server.common.dto.JobAlarmInfo;
import com.aizuda.snailjob.server.common.dto.NotifyConfigInfo;
import com.aizuda.snailjob.server.common.dto.RetryAlarmInfo;
import com.aizuda.snailjob.server.common.dto.WorkflowAlarmInfo;
import com.aizuda.snailjob.template.datasource.persistence.dataobject.JobBatchResponseDO;
import com.aizuda.snailjob.template.datasource.persistence.dataobject.WorkflowBatchResponseDO;
import com.aizuda.snailjob.template.datasource.persistence.po.JobNotifyConfig;
import com.aizuda.snailjob.template.datasource.persistence.po.NotifyConfig;
import com.aizuda.snailjob.template.datasource.persistence.po.RetryDeadLetter;
import com.aizuda.snailjob.template.datasource.persistence.po.RetryTask;
import com.aizuda.snailjob.template.datasource.persistence.po.WorkflowTaskBatch;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author xiaowoniu
 * @date 2023-12-03 10:58:53
 * @since 2.5.0
 */
@Mapper
public interface AlarmInfoConverter {

    AlarmInfoConverter INSTANCE = Mappers.getMapper(AlarmInfoConverter.class);

    @Mappings(
            @Mapping(source = "retryCount", target = "count")
    )
    List<RetryAlarmInfo> retryTaskToAlarmInfo(List<RetryTask> retryTasks);

    @Mappings(
            @Mapping(source = "retryCount", target = "count")
    )
    RetryAlarmInfo retryTaskToAlarmInfo(RetryTask retryTask);

    List<RetryAlarmInfo> deadLetterToAlarmInfo(List<RetryDeadLetter> retryDeadLetters);

    List<NotifyConfigInfo> retryToNotifyConfigInfos(List<NotifyConfig> notifyConfigs);

    @Mappings({
            @Mapping(target = "recipientIds", expression = "java(AlarmInfoConverter.toNotifyRecipientIds(notifyConfig.getRecipientIds()))")
    })
    NotifyConfigInfo retryToNotifyConfigInfos(NotifyConfig notifyConfig);

    static Set<Long> toNotifyRecipientIds(String notifyRecipientIdsStr) {
        if (StrUtil.isBlank(notifyRecipientIdsStr)) {
            return new HashSet<>();
        }

        return new HashSet<>(JsonUtil.parseList(notifyRecipientIdsStr, Long.class));
    }

    List<NotifyConfigInfo> jobToNotifyConfigInfos(List<JobNotifyConfig> notifyConfigs);

    List<JobAlarmInfo> toJobAlarmInfos(List<JobBatchResponseDO> jobBatchResponse);

    List<WorkflowAlarmInfo> toWorkflowAlarmInfos(List<WorkflowBatchResponseDO> workflowBatchResponses);

}
