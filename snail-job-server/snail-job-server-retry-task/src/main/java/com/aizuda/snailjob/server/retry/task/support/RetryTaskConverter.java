package com.aizuda.snailjob.server.retry.task.support;

import cn.hutool.core.util.StrUtil;
import com.aizuda.snailjob.client.model.DispatchRetryResultDTO;
import com.aizuda.snailjob.client.model.request.*;
import com.aizuda.snailjob.common.core.util.JsonUtil;
import com.aizuda.snailjob.server.common.dto.JobLogMetaDTO;
import com.aizuda.snailjob.server.common.dto.RetryAlarmInfo;
import com.aizuda.snailjob.server.common.dto.RetryLogMetaDTO;
import com.aizuda.snailjob.server.model.dto.RetryLogTaskDTO;
import com.aizuda.snailjob.server.model.dto.RetryTaskDTO;
import com.aizuda.snailjob.server.retry.task.dto.*;
import com.aizuda.snailjob.server.retry.task.support.generator.retry.TaskContext;
import com.aizuda.snailjob.server.retry.task.dto.RetryTaskGeneratorDTO;
import com.aizuda.snailjob.server.retry.task.support.block.BlockStrategyContext;
import com.aizuda.snailjob.server.retry.task.support.result.RetryResultContext;
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
@Mapper(imports = {com.aizuda.snailjob.server.common.util.DateUtils.class})
public interface RetryTaskConverter {

    RetryTaskConverter INSTANCE = Mappers.getMapper(RetryTaskConverter.class);

    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "deleted", ignore = true),
    })
    Retry toRetryTask(Retry retry);

    @Mappings({
            @Mapping(target = "id", ignore = true),
    })
    Retry toRetryTask(RetryDeadLetter retryDeadLetter);

    List<Retry> toRetryTaskList(List<RetryTaskDTO> retryTaskDTOList);

    Retry toRetryTask(TaskContext.TaskInfo taskInfo);

    List<RetryPartitionTask> toRetryPartitionTasks(List<Retry> retries);

    List<RetryPartitionTask> toRetryTaskLogPartitionTasks(List<Retry> retries);

    RetryTimerContext toRetryTimerContext(RetryTaskPrepareDTO retryTaskPrepareDTO);

    RetryTimerContext toRetryTimerContext(RetryTaskGeneratorDTO retryPartitionTask);

    List<NotifyConfigDTO> toNotifyConfigDTO(List<NotifyConfig> notifyConfigs);

    List<RetrySceneConfigPartitionTask> toRetrySceneConfigPartitionTask(List<RetrySceneConfig> retrySceneConfigs);

    @Mappings({
            @Mapping(target = "notifyIds", expression = "java(RetryTaskConverter.toNotifyIds(retrySceneConfig.getNotifyIds()))")
    })
    RetrySceneConfigPartitionTask toRetrySceneConfigPartitionTask(RetrySceneConfig retrySceneConfig);

    @Mappings({
            @Mapping(target = "recipientIds", expression = "java(RetryTaskConverter.toNotifyRecipientIds(notifyConfig.getRecipientIds()))")
    })
    NotifyConfigDTO toNotifyConfigDTO(NotifyConfig notifyConfig);

    static Set<Long> toNotifyIds(String notifyIdsStr) {
        if (StrUtil.isBlank(notifyIdsStr)) {
            return new HashSet<>();
        }

        return new HashSet<>(JsonUtil.parseList(notifyIdsStr, Long.class));
    }

    static Set<Long> toNotifyRecipientIds(String notifyRecipientIdsStr) {
        if (StrUtil.isBlank(notifyRecipientIdsStr)) {
            return new HashSet<>();
        }

        return new HashSet<>(JsonUtil.parseList(notifyRecipientIdsStr, Long.class));
    }

    RetryTaskLogMessage toRetryTaskLogMessage(RetryLogTaskDTO retryLogTaskDTO);

    @Mapping(target = "timestamp", expression = "java(DateUtils.toNowMilli())")
    RetryLogMetaDTO toLogMetaDTO(Retry retry);

    @Mappings({
            @Mapping(source = "reason", target = "reason"),
            @Mapping(source = "notifyScene", target = "notifyScene")
    })
    RetryTaskExecutorDTO toRetryTaskExecutorDTO(Retry retry, String reason, Integer notifyScene);

    @Mappings({
            @Mapping(source = "reason", target = "reason"),
            @Mapping(source = "notifyScene", target = "notifyScene")
    })
    RetryTaskFailAlarmEventDTO toRetryTaskFailAlarmEventDTO(Retry retry, String reason, Integer notifyScene);

    List<RetryAlarmInfo> toRetryTaskFailAlarmEventDTO(List<RetryTaskFailAlarmEventDTO> retryTaskFailAlarmEventDTOList);

    RetryTaskGeneratorDTO toRetryTaskGeneratorContext(RetryTaskPrepareDTO prepareDTO);

    RetryTask toRetryTask(RetryTaskGeneratorDTO context);

    DispatchRetryRequest toDispatchRetryRequest(RequestRetryExecutorDTO executorDTO);

    @Mappings({
            @Mapping(target = "namespaceId", source = "retry.namespaceId"),
            @Mapping(target = "groupName", source = "retry.groupName"),
            @Mapping(target = "sceneName", source = "retry.sceneName"),
            @Mapping(target = "retryId", source = "retry.id"),
            @Mapping(target = "taskType", source = "retry.taskType"),
    })
    RequestRetryExecutorDTO toRealRetryExecutorDTO(RetrySceneConfig execute, Retry retry);

    RequestRetryExecutorDTO toRealRetryExecutorDTO(TaskStopJobDTO stopJobDTO);

    RetryExecutorResultDTO toRetryExecutorResultDTO(DispatchRetryResultRequest resultDTO);

    RetryExecutorResultDTO toRetryExecutorResultDTO(DispatchCallbackResultRequest resultDTO);

    RetryExecutorResultDTO toRetryExecutorResultDTO(TaskStopJobDTO resultDTO);

    RetryExecutorResultDTO toRetryExecutorResultDTO(RequestRetryExecutorDTO resultDTO);

    RetryExecutorResultDTO toRetryExecutorResultDTO(RequestCallbackExecutorDTO resultDTO);

    RetryTaskGeneratorDTO toRetryTaskGeneratorDTO(RetryTaskPrepareDTO jobPrepareDTO);

    RetryTaskGeneratorDTO toRetryTaskGeneratorDTO(BlockStrategyContext context);

    BlockStrategyContext toBlockStrategyContext(RetryTaskPrepareDTO prepare);

    TaskStopJobDTO toTaskStopJobDTO(BlockStrategyContext context);

    TaskStopJobDTO toTaskStopJobDTO(Retry retry);

    TaskStopJobDTO toTaskStopJobDTO(RetryTaskPrepareDTO context);

    StopRetryRequest toStopRetryRequest(RequestStopRetryTaskExecutorDTO executorDTO);

    @Mappings({
            @Mapping(source = "retry.id", target = "retryId"),
    })
    RetryTaskPrepareDTO toRetryTaskPrepareDTO(Retry retry);

    @Mappings({
            @Mapping(source = "partitionTask.id", target = "retryId"),
    })
    RetryTaskPrepareDTO toRetryTaskPrepareDTO(RetryPartitionTask partitionTask);

    RetryTaskExecuteDTO toRetryTaskExecuteDTO(RetryTimerContext context);

    JobLogMetaDTO toJobLogDTO(RequestRetryExecutorDTO executorDTO);

    JobLogMetaDTO toJobLogDTO(RequestCallbackExecutorDTO executorDTO);

    RetryResultContext toRetryResultContext(RetryExecutorResultDTO resultDTO);

    @Mappings({
            @Mapping(target = "namespaceId", source = "retry.namespaceId"),
            @Mapping(target = "groupName", source = "retry.groupName"),
            @Mapping(target = "sceneName", source = "retry.sceneName"),
            @Mapping(target = "retryId", source = "retry.id"),
            @Mapping(target = "taskType", source = "retry.taskType"),
    })
    RequestCallbackExecutorDTO toRequestCallbackExecutorDTO(RetrySceneConfig retrySceneConfig, Retry retry);

    RetryCallbackRequest toRetryCallbackDTO(RequestCallbackExecutorDTO executorDTO);
}
