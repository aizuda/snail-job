package com.aizuda.snailjob.server.job.task.support.dispatch;

import akka.actor.AbstractActor;
import cn.hutool.core.collection.CollUtil;
import com.aizuda.snailjob.common.core.enums.JobTaskTypeEnum;
import com.aizuda.snailjob.common.log.SnailJobLog;
import com.aizuda.snailjob.server.common.akka.ActorGenerator;
import com.aizuda.snailjob.server.job.task.dto.ReduceTaskDTO;
import com.aizuda.snailjob.server.job.task.enums.MapReduceStageEnum;
import com.aizuda.snailjob.server.job.task.support.JobExecutor;
import com.aizuda.snailjob.server.job.task.support.JobTaskConverter;
import com.aizuda.snailjob.server.job.task.support.executor.job.JobExecutorContext;
import com.aizuda.snailjob.server.job.task.support.executor.job.JobExecutorFactory;
import com.aizuda.snailjob.server.job.task.support.generator.task.JobTaskGenerateContext;
import com.aizuda.snailjob.server.job.task.support.generator.task.JobTaskGenerator;
import com.aizuda.snailjob.server.job.task.support.generator.task.JobTaskGeneratorFactory;
import com.aizuda.snailjob.template.datasource.persistence.mapper.JobMapper;
import com.aizuda.snailjob.template.datasource.persistence.po.Job;
import com.aizuda.snailjob.template.datasource.persistence.po.JobTask;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

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
    private final JobMapper jobMapper;

    @Override
    public Receive createReceive() {
        return receiveBuilder().match(ReduceTaskDTO.class, reduceTask -> {

            try {
                doReduce(reduceTask);
            } catch (Exception e) {
                SnailJobLog.LOCAL.error("Reduce processing exception. [{}]", reduceTask, e);
            }

        }).build();
    }

    private void doReduce(final ReduceTaskDTO reduceTask) {

        Job job = jobMapper.selectById(reduceTask.getJobId());

        // 创建reduce任务
        JobTaskGenerator taskInstance = JobTaskGeneratorFactory.getTaskInstance(JobTaskTypeEnum.MAP_REDUCE.getType());
        JobTaskGenerateContext context = JobTaskConverter.INSTANCE.toJobTaskInstanceGenerateContext(job);
        context.setMrStage(MapReduceStageEnum.REDUCE);
        List<JobTask> taskList = taskInstance.generate(context);
        if (CollUtil.isEmpty(taskList)) {
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
