package com.aizuda.easy.retry.server.job.task.support;

import com.aizuda.easy.retry.client.model.request.DispatchJobRequest;
import com.aizuda.easy.retry.client.model.request.DispatchJobResultRequest;
import com.aizuda.easy.retry.server.job.task.dto.*;
import com.aizuda.easy.retry.server.job.task.support.generator.batch.JobTaskBatchGeneratorContext;
import com.aizuda.easy.retry.server.job.task.support.generator.task.JobTaskGenerateContext;
import com.aizuda.easy.retry.server.job.task.support.callback.ClientCallbackContext;
import com.aizuda.easy.retry.server.job.task.support.executor.JobExecutorContext;
import com.aizuda.easy.retry.server.job.task.support.stop.TaskStopJobContext;
import com.aizuda.easy.retry.server.job.task.support.strategy.BlockStrategies;
import com.aizuda.easy.retry.template.datasource.persistence.po.Job;
import com.aizuda.easy.retry.template.datasource.persistence.po.JobLogMessage;
import com.aizuda.easy.retry.template.datasource.persistence.po.JobTask;
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
public interface JobTaskConverter {

    JobTaskConverter INSTANCE = Mappers.getMapper(JobTaskConverter.class);

    @Mappings(
            @Mapping(source = "id", target = "jobId")
    )
    JobTaskPrepareDTO toJobTaskPrepare(JobPartitionTask job);

    @Mappings(
        @Mapping(source = "id", target = "jobId")
    )
    JobTaskPrepareDTO toJobTaskPrepare(Job job);

    JobTaskBatchGeneratorContext toJobTaskGeneratorContext(JobTaskPrepareDTO jobTaskPrepareDTO);

    JobTaskBatchGeneratorContext toJobTaskGeneratorContext(BlockStrategies.BlockStrategyContext context);

    JobTaskGenerateContext toJobTaskInstanceGenerateContext(JobExecutorContext context);

    JobTask toJobTaskInstance(JobTaskGenerateContext context);

    BlockStrategies.BlockStrategyContext toBlockStrategyContext(JobTaskPrepareDTO prepareDTO);

    TaskStopJobContext toStopJobContext(BlockStrategies.BlockStrategyContext context);

    JobLogMessage toJobLogMessage(JobLogDTO jobLogDTO);

    JobLogDTO toJobLogDTO(JobExecutorContext context);

    JobLogDTO toJobLogDTO(JobExecutorResultDTO resultDTO);

    JobLogDTO toJobLogDTO(BaseDTO baseDTO);

    JobLogDTO toJobLogDTO(DispatchJobResultRequest request);

    ClientCallbackContext toClientCallbackContext(DispatchJobResultRequest request);

    DispatchJobRequest toDispatchJobRequest(RealJobExecutorDTO realJobExecutorDTO);

    @Mappings({
            @Mapping(source = "job.groupName", target = "groupName"),
            @Mapping(source = "jobTask.id", target = "taskId"),
            @Mapping(source = "jobTask.argsStr", target = "argsStr"),
            @Mapping(source = "jobTask.argsType", target = "argsType"),
            @Mapping(source = "jobTask.extAttrs", target = "extAttrs")
    })
    RealJobExecutorDTO toRealJobExecutorDTO(Job job, JobTask jobTask);

    JobExecutorResultDTO toJobExecutorResultDTO(ClientCallbackContext context);

    @Mappings(
        @Mapping(source = "id", target = "taskId")
    )
    JobExecutorResultDTO toJobExecutorResultDTO(JobTask jobTask);

    RealStopTaskInstanceDTO toRealStopTaskInstanceDTO(TaskStopJobContext context);

    List<JobPartitionTask> toJobPartitionTasks(List<Job> jobs);

}
