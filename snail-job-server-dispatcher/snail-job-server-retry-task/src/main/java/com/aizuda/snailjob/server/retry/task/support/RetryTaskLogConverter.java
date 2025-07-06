package com.aizuda.snailjob.server.retry.task.support;

import com.aizuda.snailjob.server.common.dto.RetryLogMetaDTO;
import com.aizuda.snailjob.server.retry.task.dto.RequestCallbackExecutorDTO;
import com.aizuda.snailjob.server.retry.task.dto.RequestRetryExecutorDTO;
import com.aizuda.snailjob.server.retry.task.dto.RetryMergePartitionTaskDTO;
import com.aizuda.snailjob.server.retry.task.dto.RetryTaskLogDTO;
import com.aizuda.snailjob.template.datasource.persistence.po.Retry;
import com.aizuda.snailjob.template.datasource.persistence.po.RetryTask;
import com.aizuda.snailjob.template.datasource.persistence.po.RetryTaskLogMessage;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @author: opensnail
 * @date : 2023-05-05 16:15
 */
@Mapper
public interface RetryTaskLogConverter {

    RetryTaskLogConverter INSTANCE = Mappers.getMapper(RetryTaskLogConverter.class);

    @Mappings({
            @Mapping(target = "id", ignore = true),
    })
    RetryTask toRetryTask(Retry retry);

    RetryTaskLogDTO toRetryTaskLogDTO(Retry retry);

    RetryTaskLogDTO toRetryTaskLogDTO(RequestRetryExecutorDTO retry);

    List<RetryMergePartitionTaskDTO> toRetryMergePartitionTaskDTOs(List<RetryTask> retryTaskList);

    RetryTaskLogMessage toRetryTaskLogMessage(RetryTaskLogMessage message);

    RetryLogMetaDTO toRetryLogMetaDTO(RequestRetryExecutorDTO executorDTO);

    RetryLogMetaDTO toRetryLogMetaDTO(RequestCallbackExecutorDTO executorDTO);
}
