package com.aizuda.snailjob.server.job.task.support;

import com.aizuda.snailjob.server.job.task.dto.WorkflowPartitionTaskDTO;
import com.aizuda.snailjob.server.job.task.dto.WorkflowTaskPrepareDTO;
import com.aizuda.snailjob.server.job.task.support.block.workflow.WorkflowBlockStrategyContext;
import com.aizuda.snailjob.server.job.task.support.executor.workflow.WorkflowExecutorContext;
import com.aizuda.snailjob.server.job.task.support.generator.batch.JobTaskBatchGeneratorContext;
import com.aizuda.snailjob.server.job.task.support.generator.batch.WorkflowTaskBatchGeneratorContext;
import com.aizuda.snailjob.server.model.dto.CallbackParamsDTO;
import com.aizuda.snailjob.template.datasource.persistence.po.JobTask;
import com.aizuda.snailjob.template.datasource.persistence.po.Workflow;
import com.aizuda.snailjob.template.datasource.persistence.po.WorkflowNode;
import com.aizuda.snailjob.template.datasource.persistence.po.WorkflowTaskBatch;
import com.aizuda.snailjob.server.job.task.support.block.workflow.WorkflowBlockStrategyContext;
import com.aizuda.snailjob.server.job.task.support.executor.workflow.WorkflowExecutorContext;
import com.aizuda.snailjob.server.job.task.support.generator.batch.JobTaskBatchGeneratorContext;
import com.aizuda.snailjob.server.job.task.support.generator.batch.WorkflowTaskBatchGeneratorContext;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
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

    @Mappings(
        @Mapping(source = "id", target = "workflowId")
    )
    WorkflowTaskPrepareDTO toWorkflowTaskPrepareDTO(WorkflowPartitionTaskDTO workflowPartitionTaskDTO);

    @Mappings(
            @Mapping(source = "id", target = "workflowId")
    )
    WorkflowTaskPrepareDTO toWorkflowTaskPrepareDTO(Workflow workflow);

    WorkflowTaskBatchGeneratorContext toWorkflowTaskBatchGeneratorContext(WorkflowTaskPrepareDTO workflowTaskPrepareDTO);

    WorkflowTaskBatch toWorkflowTaskBatch(WorkflowTaskBatchGeneratorContext context);

    JobTaskBatchGeneratorContext toJobTaskBatchGeneratorContext(WorkflowExecutorContext context);

    @Mappings(
        @Mapping(source = "id", target = "workflowNodeId")
    )
    WorkflowExecutorContext toWorkflowExecutorContext(WorkflowNode workflowNode);

    WorkflowTaskBatchGeneratorContext toWorkflowTaskBatchGeneratorContext(WorkflowBlockStrategyContext context);

    WorkflowBlockStrategyContext toWorkflowBlockStrategyContext(WorkflowTaskPrepareDTO prepareDTO);

    List<CallbackParamsDTO> toCallbackParamsDTO(List<JobTask> tasks);
}
