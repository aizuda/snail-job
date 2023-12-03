package com.aizuda.easy.retry.server.common;

import com.aizuda.easy.retry.server.common.alarm.AbstractRetryAlarm;
import com.aizuda.easy.retry.server.common.dto.JobAlarmInfo;
import com.aizuda.easy.retry.server.common.dto.NotifyConfigInfo;
import com.aizuda.easy.retry.server.common.dto.RetryAlarmInfo;
import com.aizuda.easy.retry.template.datasource.persistence.dataobject.JobBatchResponseDO;
import com.aizuda.easy.retry.template.datasource.persistence.po.JobNotifyConfig;
import com.aizuda.easy.retry.template.datasource.persistence.po.NotifyConfig;
import com.aizuda.easy.retry.template.datasource.persistence.po.RetryDeadLetter;
import com.aizuda.easy.retry.template.datasource.persistence.po.RetryTask;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @author xiaowoniu
 * @date 2023-12-03 10:58:53
 * @since 2.5.0
 */
@Mapper
public interface AlarmInfoConverter {

    AlarmInfoConverter INSTANCE = Mappers.getMapper(AlarmInfoConverter.class);

    List<RetryAlarmInfo> retryTaskToAlarmInfo(List<RetryTask> retryTasks);

    List<RetryAlarmInfo> deadLetterToAlarmInfo(List<RetryDeadLetter> retryDeadLetters);

    List<NotifyConfigInfo> retryToNotifyConfigInfos(List<NotifyConfig> notifyConfigs);

    List<NotifyConfigInfo> jobToNotifyConfigInfos(List<JobNotifyConfig> notifyConfigs);

    List<JobAlarmInfo> toJobAlarmInfos(List<JobBatchResponseDO> jobBatchResponse);

}
