package com.aizuda.snailjob.server.retry.task.support;

import cn.hutool.core.util.StrUtil;
import com.aizuda.snailjob.common.core.util.JsonUtil;
import com.aizuda.snailjob.server.common.dto.RetryLogMetaDTO;
import com.aizuda.snailjob.server.model.dto.RetryLogTaskDTO;
import com.aizuda.snailjob.server.model.dto.RetryTaskDTO;
import com.aizuda.snailjob.server.retry.task.dto.NotifyConfigPartitionTask;
import com.aizuda.snailjob.server.retry.task.dto.RetryPartitionTask;
import com.aizuda.snailjob.server.retry.task.generator.task.TaskContext;
import com.aizuda.snailjob.server.retry.task.support.timer.RetryTimerContext;
import com.aizuda.snailjob.template.datasource.persistence.po.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    RetryTask toRetryTask(TaskContext.TaskInfo taskInfo);

    List<RetryPartitionTask> toRetryPartitionTasks(List<RetryTask> retryTasks);

    List<RetryPartitionTask> toRetryTaskLogPartitionTasks(List<RetryTaskLog> retryTaskLogList);

    RetryTimerContext toRetryTimerContext(RetryPartitionTask retryPartitionTask);

    List<NotifyConfigPartitionTask> toNotifyConfigPartitionTask(List<NotifyConfig> notifyConfigs);

    @Mappings({
            @Mapping(target = "recipientIds", expression = "java(RetryTaskConverter.toNotifyRecipientIds(notifyConfig.getRecipientIds()))")
    })
    NotifyConfigPartitionTask toNotifyConfigPartitionTask(NotifyConfig notifyConfig);

    static Set<Long> toNotifyRecipientIds(String notifyRecipientIdsStr) {
        if (StrUtil.isBlank(notifyRecipientIdsStr)) {
            return new HashSet<>();
        }

        return new HashSet<>(JsonUtil.parseList(notifyRecipientIdsStr, Long.class));
    }

    RetryTaskLogMessage toRetryTaskLogMessage(RetryLogTaskDTO retryLogTaskDTO);

    RetryLogMetaDTO toLogMetaDTO(RetryTask retryTask);

}
