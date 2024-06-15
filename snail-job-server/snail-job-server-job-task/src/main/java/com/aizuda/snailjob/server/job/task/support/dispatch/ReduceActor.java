package com.aizuda.snailjob.server.job.task.support.dispatch;

import akka.actor.AbstractActor;
import cn.hutool.core.collection.CollUtil;
import com.aizuda.snailjob.common.core.enums.JobTaskTypeEnum;
import com.aizuda.snailjob.common.core.enums.MapReduceStageEnum;
import com.aizuda.snailjob.common.core.util.JsonUtil;
import com.aizuda.snailjob.common.log.SnailJobLog;
import com.aizuda.snailjob.server.common.akka.ActorGenerator;
import com.aizuda.snailjob.server.job.task.dto.ReduceTaskDTO;
import com.aizuda.snailjob.server.job.task.support.JobExecutor;
import com.aizuda.snailjob.server.job.task.support.JobTaskConverter;
import com.aizuda.snailjob.server.job.task.support.executor.job.JobExecutorContext;
import com.aizuda.snailjob.server.job.task.support.executor.job.JobExecutorFactory;
import com.aizuda.snailjob.server.job.task.support.generator.task.JobTaskGenerateContext;
import com.aizuda.snailjob.server.job.task.support.generator.task.JobTaskGenerator;
import com.aizuda.snailjob.server.job.task.support.generator.task.JobTaskGeneratorFactory;
import com.aizuda.snailjob.server.job.task.support.handler.DistributedLockHandler;
import com.aizuda.snailjob.template.datasource.persistence.mapper.JobMapper;
import com.aizuda.snailjob.template.datasource.persistence.mapper.JobTaskMapper;
import com.aizuda.snailjob.template.datasource.persistence.po.Job;
import com.aizuda.snailjob.template.datasource.persistence.po.JobTask;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;
import java.time.Duration;
import java.util.List;

/**
 * 负责生成reduce任务并执行
 *
 * @author: opensnail
 * @date : 2024-06-12
 * @since : sj_1.1.0
 */
@Component(ActorGenerator.JOB_REDUCE_ACTOR)
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@RequiredArgsConstructor
public class ReduceActor extends AbstractActor {
    private static final String KEY = "job_generate_reduce_{0}_{1}";
    private final DistributedLockHandler distributedLockHandler;
    private final JobMapper jobMapper;
    private final JobTaskMapper jobTaskMapper;

    @Override
    public Receive createReceive() {
        return receiveBuilder().match(ReduceTaskDTO.class, reduceTask -> {
            SnailJobLog.LOCAL.info("执行Reduce, [{}]", JsonUtil.toJsonString(reduceTask));
            try {
                distributedLockHandler.lockWithDisposableAndRetry(() -> {
                            doReduce(reduceTask);
                        }, MessageFormat.format(KEY, reduceTask.getTaskBatchId(), reduceTask.getJobId()),
                        Duration.ofSeconds(1),
                        Duration.ofSeconds(1),
                        3);
            } catch (Exception e) {
                SnailJobLog.LOCAL.error("Reduce processing exception. [{}]", reduceTask, e);
            }

        }).build();
    }

    private void doReduce(final ReduceTaskDTO reduceTask) {

        List<JobTask> jobTasks = jobTaskMapper.selectList(new PageDTO<>(1, 1),
                new LambdaQueryWrapper<JobTask>()
                        .select(JobTask::getId)
                        .eq(JobTask::getTaskBatchId, reduceTask.getTaskBatchId())
                        .eq(JobTask::getMrStage, MapReduceStageEnum.REDUCE.getStage())
        );

        if (CollUtil.isNotEmpty(jobTasks)) {
            // 说明已经创建了reduce任务了
            return;
        }

        Job job = jobMapper.selectById(reduceTask.getJobId());
        // 非MAP_REDUCE任务不处理
        if (JobTaskTypeEnum.MAP_REDUCE.getType() != job.getTaskType()) {
            return;
        }

        // 创建reduce任务
        JobTaskGenerator taskInstance = JobTaskGeneratorFactory.getTaskInstance(JobTaskTypeEnum.MAP_REDUCE.getType());
        JobTaskGenerateContext context = JobTaskConverter.INSTANCE.toJobTaskInstanceGenerateContext(job);
        context.setTaskBatchId(reduceTask.getTaskBatchId());
        context.setMrStage(MapReduceStageEnum.REDUCE.getStage());
        List<JobTask> taskList = taskInstance.generate(context);
        if (CollUtil.isEmpty(taskList)) {
            SnailJobLog.LOCAL.warn("Job task is empty, taskBatchId:[{}]", reduceTask.getTaskBatchId());
            return;
        }

        // 执行任务
        JobExecutor jobExecutor = JobExecutorFactory.getJobExecutor(JobTaskTypeEnum.MAP_REDUCE.getType());
        jobExecutor.execute(buildJobExecutorContext(reduceTask, job, taskList));

    }

    private static JobExecutorContext buildJobExecutorContext(ReduceTaskDTO reduceTask, Job job,
                                                              List<JobTask> taskList) {
        JobExecutorContext context = JobTaskConverter.INSTANCE.toJobExecutorContext(job);
        context.setTaskList(taskList);
        context.setTaskBatchId(reduceTask.getTaskBatchId());
        context.setWorkflowTaskBatchId(reduceTask.getWorkflowTaskBatchId());
        context.setWorkflowNodeId(reduceTask.getWorkflowNodeId());
        return context;
    }
}
