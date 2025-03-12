package com.aizuda.snailjob.server.job.task.support.stop;

import  org.apache.pekko.actor.ActorRef;
import cn.hutool.core.collection.CollUtil;
import com.aizuda.snailjob.common.core.enums.JobTaskBatchStatusEnum;
import com.aizuda.snailjob.common.core.enums.JobTaskStatusEnum;
import com.aizuda.snailjob.server.common.akka.ActorGenerator;
import com.aizuda.snailjob.server.job.task.dto.JobExecutorResultDTO;
import com.aizuda.snailjob.server.job.task.support.JobTaskConverter;
import com.aizuda.snailjob.server.job.task.support.JobTaskStopHandler;
import com.aizuda.snailjob.template.datasource.persistence.mapper.JobTaskBatchMapper;
import com.aizuda.snailjob.template.datasource.persistence.mapper.JobTaskMapper;
import com.aizuda.snailjob.template.datasource.persistence.po.JobTask;
import com.aizuda.snailjob.template.datasource.persistence.po.JobTaskBatch;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * @author opensnail
 * @date 2023-10-03 09:48:10
 * @since 2.4.0
 */
public abstract class AbstractJobTaskStopHandler implements JobTaskStopHandler, InitializingBean {

    @Autowired
    private JobTaskMapper jobTaskMapper;
    @Autowired
    private JobTaskBatchMapper jobTaskBatchMapper;

    protected abstract void doStop(TaskStopJobContext context);

    @Override
    public void stop(TaskStopJobContext context) {

        LambdaQueryWrapper<JobTask> queryWrapper = new LambdaQueryWrapper<JobTask>()
                .eq(JobTask::getTaskBatchId, context.getTaskBatchId());

        if (!context.isForceStop()) {
            queryWrapper.in(JobTask::getTaskStatus, JobTaskStatusEnum.NOT_COMPLETE);
        }

        List<JobTask> jobTasks = jobTaskMapper.selectList(queryWrapper);

        if (CollUtil.isEmpty(jobTasks)) {
            // 若没有任务项，直接变更状态为已停止
            JobTaskBatch jobTaskBatch = new JobTaskBatch();
            jobTaskBatch.setId(context.getTaskBatchId());
            jobTaskBatch.setTaskBatchStatus(JobTaskBatchStatusEnum.STOP.getStatus());
            jobTaskBatch.setOperationReason(context.getJobOperationReason());
            jobTaskBatchMapper.updateById(jobTaskBatch);
            return;
        }

        context.setJobTasks(jobTasks);

        doStop(context);

        if (context.isNeedUpdateTaskStatus()) {
            for (final JobTask jobTask : jobTasks) {
                if (jobTask.getTaskStatus() == JobTaskStatusEnum.SUCCESS.getStatus()){
                    continue;
                }
                JobExecutorResultDTO jobExecutorResultDTO = JobTaskConverter.INSTANCE.toJobExecutorResultDTO(jobTask);
                jobExecutorResultDTO.setTaskStatus(JobTaskStatusEnum.STOP.getStatus());
                jobExecutorResultDTO.setMessage("任务停止成功");
                jobExecutorResultDTO.setJobOperationReason(context.getJobOperationReason());
                jobExecutorResultDTO.setTaskType(getTaskType().getType());
                jobExecutorResultDTO.setWorkflowNodeId(context.getWorkflowNodeId());
                jobExecutorResultDTO.setWorkflowTaskBatchId(context.getWorkflowTaskBatchId());
                ActorRef actorRef = ActorGenerator.jobTaskExecutorResultActor();
                actorRef.tell(jobExecutorResultDTO, actorRef);
            }

        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        JobTaskStopFactory.registerTaskStop(getTaskType(), this);
    }
}
