package com.aizuda.snailjob.server.web.service.convert;

import com.aizuda.snailjob.server.model.dto.RetryTaskDTO;
import com.aizuda.snailjob.server.retry.task.generator.task.TaskContext;
import com.aizuda.snailjob.server.web.model.request.RetryTaskSaveRequestVO;
import com.aizuda.snailjob.server.retry.task.generator.task.TaskContext;
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
