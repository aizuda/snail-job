package com.aizuda.easy.retry.server.job.task.support;

import com.aizuda.easy.retry.server.job.task.dto.JobPartitionTaskDTO;
import com.aizuda.easy.retry.server.job.task.dto.WorkflowPartitionTaskDTO;
import com.aizuda.easy.retry.server.job.task.dto.WorkflowTaskPrepareDTO;
import com.aizuda.easy.retry.template.datasource.persistence.po.Workflow;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @author xiaowoniu
 * @date 2023-12-21 22:04:19
 * @since 2.6.0
 */
@Mapper
public interface WorkflowTaskConverter {
    WorkflowTaskConverter INSTANCE = Mappers.getMapper(WorkflowTaskConverter.class);

    List<WorkflowPartitionTaskDTO> toWorkflowPartitionTaskList(List<Workflow> workflowList);

    WorkflowTaskPrepareDTO toWorkflowTaskPrepareDTO(WorkflowPartitionTaskDTO workflowPartitionTaskDTO);
}
