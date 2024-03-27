package com.aizuda.easy.retry.server.retry.task.support;

import com.aizuda.easy.retry.server.retry.task.dto.RetryMergePartitionTaskDTO;
import com.aizuda.easy.retry.server.retry.task.support.dispatch.actor.log.RetryTaskLogDTO;
import com.aizuda.easy.retry.template.datasource.persistence.po.RetryTask;
import com.aizuda.easy.retry.template.datasource.persistence.po.RetryTaskLog;
import com.aizuda.easy.retry.template.datasource.persistence.po.RetryTaskLogMessage;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @author: www.byteblogs.com
 * @date : 2023-05-05 16:15
 */
@Mapper
public interface RetryTaskLogConverter {

    RetryTaskLogConverter INSTANCE = Mappers.getMapper(RetryTaskLogConverter.class);

    @Mappings({
        @Mapping(target = "id", ignore = true),
    })
    RetryTaskLog toRetryTask(RetryTask retryTask);

    RetryTaskLogDTO toRetryTaskLogDTO(RetryTask retryTask);

    List<RetryMergePartitionTaskDTO> toRetryMergePartitionTaskDTOs(List<RetryTaskLog> retryTaskLogList);

    RetryTaskLogMessage toRetryTaskLogMessage(RetryTaskLogMessage message);
}
