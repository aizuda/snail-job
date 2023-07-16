package com.aizuda.easy.retry.server.service.convert;

import com.aizuda.easy.retry.server.model.dto.RetryTaskDTO;
import com.aizuda.easy.retry.server.persistence.mybatis.po.RetryDeadLetter;
import com.aizuda.easy.retry.server.persistence.mybatis.po.RetryTask;
import com.aizuda.easy.retry.server.support.generator.task.TaskContext;
import com.aizuda.easy.retry.server.web.model.request.RetryTaskSaveRequestVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @author: www.byteblogs.com
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

    RetryTask toRetryTask(RetryTaskSaveRequestVO retryTaskSaveRequestVO);

    List<RetryTask> toRetryTaskList(List<RetryTaskDTO> retryTaskDTOList);

    RetryTask toRetryTask(TaskContext.TaskInfo taskInfo);
}
