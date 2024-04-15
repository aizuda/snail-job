package com.aizuda.snail.job.server.web.service.convert;

import com.aizuda.snail.job.server.model.dto.RetryTaskDTO;
import com.aizuda.snail.job.server.retry.task.generator.task.TaskContext;
import com.aizuda.snail.job.server.web.model.request.RetryTaskSaveRequestVO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @author opensnail
 * @date 2023-07-16 22:09:40
 * @since 2.1.0
 */
@Mapper
public interface TaskContextConverter {
    TaskContextConverter INSTANCE = Mappers.getMapper(TaskContextConverter.class);

    TaskContext.TaskInfo toTaskContextInfo(RetryTaskSaveRequestVO retryTaskSaveRequestVO);

    List<TaskContext.TaskInfo> toTaskContextInfo(List<RetryTaskDTO> retryTasks);
}
