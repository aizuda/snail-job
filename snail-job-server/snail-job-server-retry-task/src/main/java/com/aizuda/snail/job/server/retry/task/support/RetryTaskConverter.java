package com.aizuda.snail.job.server.retry.task.support;

import com.aizuda.snail.job.server.model.dto.RetryLogTaskDTO;
import com.aizuda.snail.job.server.model.dto.RetryTaskDTO;
import com.aizuda.snail.job.server.common.dto.RetryLogMetaDTO;
import com.aizuda.snail.job.server.retry.task.dto.NotifyConfigPartitionTask;
import com.aizuda.snail.job.server.retry.task.dto.RetryPartitionTask;
import com.aizuda.snail.job.server.retry.task.support.timer.RetryTimerContext;
import com.aizuda.snail.job.server.retry.task.generator.task.TaskContext.TaskInfo;
import com.aizuda.snail.job.template.datasource.persistence.po.NotifyConfig;
import com.aizuda.snail.job.template.datasource.persistence.po.RetryDeadLetter;
import com.aizuda.snail.job.template.datasource.persistence.po.RetryTask;
import com.aizuda.snail.job.template.datasource.persistence.po.RetryTaskLog;
import com.aizuda.snail.job.template.datasource.persistence.po.RetryTaskLogMessage;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @author: opensnail
 * @date : 2021-11-26 15:22
 */
@Mapper
public interface RetryTaskConverter {

    RetryTaskConverter INSTANCE = Mappers.getMapper(RetryTaskConverter.class);

    RetryTask toRetryTask(RetryTaskDTO retryTaskDTO);

    RetryTask toRetryTask(RetryTask retryTask);

    @Mappings({
            @Mapping(target = "id", ignore = true),
    })
    RetryTask toRetryTask(RetryDeadLetter retryDeadLetter);

    List<RetryTask> toRetryTaskList(List<RetryTaskDTO> retryTaskDTOList);

    RetryTask toRetryTask(TaskInfo taskInfo);

    List<RetryPartitionTask> toRetryPartitionTasks(List<RetryTask> retryTasks);

    List<RetryPartitionTask> toRetryTaskLogPartitionTasks(List<RetryTaskLog> retryTaskLogList);

    RetryTimerContext toRetryTimerContext(RetryPartitionTask retryPartitionTask);

    List<NotifyConfigPartitionTask> toNotifyConfigPartitionTask(List<NotifyConfig> notifyConfigs);

    RetryTaskLogMessage toRetryTaskLogMessage(RetryLogTaskDTO retryLogTaskDTO);

    RetryLogMetaDTO toLogMetaDTO(RetryTask retryTask);

}
