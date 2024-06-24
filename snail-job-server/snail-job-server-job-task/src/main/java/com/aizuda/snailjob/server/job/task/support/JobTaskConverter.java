package com.aizuda.snailjob.server.job.task.support;

import com.aizuda.snailjob.client.model.request.DispatchJobRequest;
import com.aizuda.snailjob.client.model.request.DispatchJobResultRequest;
import com.aizuda.snailjob.client.model.request.MapTaskRequest;
import com.aizuda.snailjob.server.common.dto.JobAlarmInfo;
import com.aizuda.snailjob.server.common.dto.JobLogMetaDTO;
import com.aizuda.snailjob.server.job.task.dto.*;
import com.aizuda.snailjob.server.job.task.support.alarm.event.JobTaskFailAlarmEvent;
import com.aizuda.snailjob.server.job.task.support.block.job.BlockStrategyContext;
import com.aizuda.snailjob.server.job.task.support.callback.ClientCallbackContext;
import com.aizuda.snailjob.server.job.task.support.executor.job.JobExecutorContext;
import com.aizuda.snailjob.server.job.task.support.executor.workflow.WorkflowExecutorContext;
import com.aizuda.snailjob.server.job.task.support.generator.batch.JobTaskBatchGeneratorContext;
import com.aizuda.snailjob.server.job.task.support.generator.task.JobTaskGenerateContext;
import com.aizuda.snailjob.server.job.task.support.stop.TaskStopJobContext;
import com.aizuda.snailjob.server.model.dto.LogTaskDTO;
import com.aizuda.snailjob.template.datasource.persistence.po.Job;
import com.aizuda.snailjob.template.datasource.persistence.po.JobLogMessage;
import com.aizuda.snailjob.template.datasource.persistence.po.JobTask;
import com.aizuda.snailjob.template.datasource.persistence.po.JobTaskBatch;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @author: opensnail
 * @date : 2021-11-26 15:22
 * @since : 2.5.0
 */
@Mapper
public interface JobTaskConverter {

    JobTaskConverter INSTANCE = Mappers.getMapper(JobTaskConverter.class);

    @Mappings(
            @Mapping(source = "id", target = "jobId")
    )
    JobTaskPrepareDTO toJobTaskPrepare(JobPartitionTaskDTO job);

    @Mappings(
            @Mapping(source = "id", target = "jobId")
    )
    JobTaskPrepareDTO toJobTaskPrepare(Job job);

    @Mappings({
            @Mapping(source = "job.id", target = "jobId"),
            @Mapping(source = "job.namespaceId", target = "namespaceId"),
            @Mapping(source = "job.groupName", target = "groupName")
    })
    JobTaskPrepareDTO toJobTaskPrepare(Job job, WorkflowExecutorContext context);

    JobTaskBatchGeneratorContext toJobTaskGeneratorContext(JobTaskPrepareDTO jobTaskPrepareDTO);

    JobTaskBatchGeneratorContext toJobTaskGeneratorContext(BlockStrategyContext context);

    @Mappings(
            @Mapping(source = "id", target = "jobId")
    )
    JobTaskGenerateContext toJobTaskInstanceGenerateContext(Job job);

    JobTaskGenerateContext toJobTaskInstanceGenerateContext(MapTaskRequest request);

    JobTask toJobTaskInstance(JobTaskGenerateContext context);

    BlockStrategyContext toBlockStrategyContext(JobTaskPrepareDTO prepareDTO);

    TaskStopJobContext toStopJobContext(BlockStrategyContext context);

    TaskStopJobContext toStopJobContext(JobExecutorResultDTO context);

    @Mappings(
            @Mapping(source = "id", target = "jobId")
    )
    TaskStopJobContext toStopJobContext(Job job);

    TaskStopJobContext toStopJobContext(JobTaskPrepareDTO context);

    JobLogMessage toJobLogMessage(JobLogDTO jobLogDTO);

    JobLogMessage toJobLogMessage(LogTaskDTO logTaskDTO);

    JobLogMetaDTO toJobLogDTO(BaseDTO baseDTO);

    ClientCallbackContext toClientCallbackContext(DispatchJobResultRequest request);

    ClientCallbackContext toClientCallbackContext(RealJobExecutorDTO request);

    @Mappings(
            @Mapping(source = "id", target = "jobId")
    )
    ClientCallbackContext toClientCallbackContext(Job job);

    DispatchJobRequest toDispatchJobRequest(RealJobExecutorDTO realJobExecutorDTO);

    @Mappings({
            @Mapping(source = "jobTask.groupName", target = "groupName"),
            @Mapping(source = "jobTask.jobId", target = "jobId"),
            @Mapping(source = "jobTask.taskBatchId", target = "taskBatchId"),
            @Mapping(source = "jobTask.id", target = "taskId"),
            @Mapping(source = "jobTask.argsStr", target = "argsStr"),
            @Mapping(source = "jobTask.argsType", target = "argsType"),
            @Mapping(source = "jobTask.extAttrs", target = "extAttrs"),
            @Mapping(source = "jobTask.namespaceId", target = "namespaceId")
    })
    RealJobExecutorDTO toRealJobExecutorDTO(JobExecutorContext context, JobTask jobTask);

    @Mappings(
            @Mapping(source = "id", target = "jobId")
    )
    JobExecutorContext toJobExecutorContext(Job job);

    JobExecutorResultDTO toJobExecutorResultDTO(ClientCallbackContext context);

    @Mappings(
            @Mapping(source = "id", target = "taskId")
    )
    JobExecutorResultDTO toJobExecutorResultDTO(JobTask jobTask);

    JobExecutorResultDTO toJobExecutorResultDTO(RealJobExecutorDTO realJobExecutorDTO);

    RealStopTaskInstanceDTO toRealStopTaskInstanceDTO(TaskStopJobContext context);

    List<JobPartitionTaskDTO> toJobPartitionTasks(List<Job> jobs);

    List<JobPartitionTaskDTO> toJobTaskBatchPartitionTasks(List<JobTaskBatch> jobTaskBatches);

    JobTaskBatch toJobTaskBatch(JobTaskBatchGeneratorContext context);

    CompleteJobBatchDTO toCompleteJobBatchDTO(JobExecutorResultDTO jobExecutorResultDTO);

    CompleteJobBatchDTO completeJobBatchDTO(JobTaskPrepareDTO jobTaskPrepareDTO);

    JobLogMessage toJobLogMessage(JobLogMessage jobLogMessage);

    ReduceTaskDTO toReduceTaskDTO(CompleteJobBatchDTO jobBatchDTO);
}
