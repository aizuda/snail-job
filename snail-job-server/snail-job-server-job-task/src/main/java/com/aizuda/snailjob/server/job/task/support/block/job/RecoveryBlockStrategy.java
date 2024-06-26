package com.aizuda.snailjob.server.job.task.support.block.job;

import akka.actor.ActorRef;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import com.aizuda.snailjob.common.core.enums.JobTaskStatusEnum;
import com.aizuda.snailjob.common.core.util.StreamUtils;
import com.aizuda.snailjob.common.log.SnailJobLog;
import com.aizuda.snailjob.server.common.akka.ActorGenerator;
import com.aizuda.snailjob.server.common.enums.JobTaskExecutorSceneEnum;
import com.aizuda.snailjob.server.common.exception.SnailJobServerException;
import com.aizuda.snailjob.server.job.task.dto.TaskExecuteDTO;
import com.aizuda.snailjob.server.job.task.enums.BlockStrategyEnum;
import com.aizuda.snailjob.server.job.task.support.JobExecutor;
import com.aizuda.snailjob.server.job.task.support.JobTaskConverter;
import com.aizuda.snailjob.server.job.task.support.executor.job.JobExecutorContext;
import com.aizuda.snailjob.server.job.task.support.executor.job.JobExecutorFactory;
import com.aizuda.snailjob.template.datasource.persistence.mapper.JobMapper;
import com.aizuda.snailjob.template.datasource.persistence.mapper.JobTaskMapper;
import com.aizuda.snailjob.template.datasource.persistence.po.Job;
import com.aizuda.snailjob.template.datasource.persistence.po.JobTask;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Stream;

/**
 * 重新触发执行失败的任务
 *
 * @author opensnail
 * @date 2024-06-16 11:30:59
 * @since sj_1.1.0
 */
@Component
@RequiredArgsConstructor
public class RecoveryBlockStrategy extends AbstracJobBlockStrategy {

    private final JobTaskMapper jobTaskMapper;
    private final JobMapper jobMapper;

    @Override
    protected void doBlock(BlockStrategyContext context) {
        Assert.notNull(context.getJobId(), () -> new SnailJobServerException("job id can not be null"));
        Assert.notNull(context.getTaskBatchId(), () -> new SnailJobServerException("task batch id can not be null"));
        Assert.notNull(context.getTaskType(), () -> new SnailJobServerException("task type can not be null"));

        List<JobTask> jobTasks = jobTaskMapper.selectList(
            new LambdaQueryWrapper<JobTask>()
                .eq(JobTask::getTaskBatchId, context.getTaskBatchId())
        );

        //  若任务项为空则生成任务项
        if (CollUtil.isEmpty(jobTasks)) {
            TaskExecuteDTO taskExecuteDTO = new TaskExecuteDTO();
            taskExecuteDTO.setTaskBatchId(context.getTaskBatchId());
            taskExecuteDTO.setJobId(context.getJobId());
            taskExecuteDTO.setTaskExecutorScene(JobTaskExecutorSceneEnum.MANUAL_JOB.getType());
            taskExecuteDTO.setWorkflowTaskBatchId(context.getWorkflowTaskBatchId());
            taskExecuteDTO.setWorkflowNodeId(context.getWorkflowNodeId());
            ActorRef actorRef = ActorGenerator.jobTaskExecutorActor();
            actorRef.tell(taskExecuteDTO, actorRef);
            return;
        }

        Job job = jobMapper.selectById(context.getJobId());
        // 执行任务 Stop or Fail 任务
        JobExecutor jobExecutor = JobExecutorFactory.getJobExecutor(context.getTaskType());
        jobExecutor.execute(buildJobExecutorContext(context, job,
            StreamUtils.filter(jobTasks,
                (jobTask) -> JobTaskStatusEnum.NOT_SUCCESS.contains(jobTask.getTaskStatus())
                             || JobTaskStatusEnum.NOT_COMPLETE.contains(jobTask.getTaskStatus()))));
    }

    @Override
    protected BlockStrategyEnum blockStrategyEnum() {
        return BlockStrategyEnum.RECOVERY;
    }

    private static JobExecutorContext buildJobExecutorContext(BlockStrategyContext strategyContext, Job job,
        List<JobTask> taskList) {
        JobExecutorContext context = JobTaskConverter.INSTANCE.toJobExecutorContext(job);
        context.setTaskList(taskList);
        context.setTaskBatchId(strategyContext.getTaskBatchId());
        context.setWorkflowTaskBatchId(strategyContext.getWorkflowTaskBatchId());
        context.setWorkflowNodeId(strategyContext.getWorkflowNodeId());
        return context;
    }

}
