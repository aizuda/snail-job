package com.aizuda.snailjob.server.job.task.support.dispatch;

import  org.apache.pekko.actor.AbstractActor;
import cn.hutool.core.collection.CollUtil;
import com.aizuda.snailjob.common.core.enums.JobTaskBatchStatusEnum;
import com.aizuda.snailjob.server.common.pekko.ActorGenerator;
import com.aizuda.snailjob.server.common.enums.JobTaskExecutorSceneEnum;
import com.aizuda.snailjob.server.common.enums.SyetemTaskTypeEnum;
import com.aizuda.snailjob.server.job.task.dto.JobTaskPrepareDTO;
import com.aizuda.snailjob.server.job.task.support.JobPrepareHandler;
import com.aizuda.snailjob.template.datasource.persistence.mapper.JobTaskBatchMapper;
import com.aizuda.snailjob.template.datasource.persistence.po.JobTaskBatch;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.aizuda.snailjob.common.core.enums.JobTaskBatchStatusEnum.NOT_COMPLETE;

/**
 * 调度任务准备阶段
 *
 * @author opensnail
 * @date 2023-09-25 22:20:53
 * @since 2.4.0
 */
@Component(ActorGenerator.JOB_TASK_PREPARE_ACTOR)
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Slf4j
@RequiredArgsConstructor
public class JobTaskPrepareActor extends AbstractActor {
    private final JobTaskBatchMapper jobTaskBatchMapper;
    private final List<JobPrepareHandler> prepareHandlers;

    @Override
    public Receive createReceive() {
        return receiveBuilder().match(JobTaskPrepareDTO.class, job -> {
            try {
                doPrepare(job);
            } catch (Exception e) {
                log.error("预处理节点异常", e);
            } finally {
                getContext().stop(getSelf());
            }
        }).build();
    }

    private void doPrepare(JobTaskPrepareDTO prepare) {
        LambdaQueryWrapper<JobTaskBatch> queryWrapper = new LambdaQueryWrapper<JobTaskBatch>()
                .eq(JobTaskBatch::getJobId, prepare.getJobId())
                .in(JobTaskBatch::getTaskBatchStatus, NOT_COMPLETE);

        JobTaskExecutorSceneEnum jobTaskExecutorSceneEnum = JobTaskExecutorSceneEnum.get(
                prepare.getTaskExecutorScene());
        if (SyetemTaskTypeEnum.WORKFLOW.getType().equals(jobTaskExecutorSceneEnum.getSystemTaskType().getType())) {
            queryWrapper.eq(JobTaskBatch::getWorkflowNodeId, prepare.getWorkflowNodeId());
            queryWrapper.eq(JobTaskBatch::getWorkflowTaskBatchId, prepare.getWorkflowTaskBatchId());
            queryWrapper.eq(JobTaskBatch::getSystemTaskType, SyetemTaskTypeEnum.WORKFLOW.getType());
        } else {
            queryWrapper.eq(JobTaskBatch::getSystemTaskType, SyetemTaskTypeEnum.JOB.getType());
        }

        List<JobTaskBatch> notCompleteJobTaskBatchList = jobTaskBatchMapper
                .selectList(queryWrapper);

        // 说明所以任务已经完成
        if (CollUtil.isEmpty(notCompleteJobTaskBatchList)) {
            JobTaskBatch jobTaskBatch = new JobTaskBatch();
            // 模拟完成情况
            jobTaskBatch.setTaskBatchStatus(JobTaskBatchStatusEnum.SUCCESS.getStatus());
            notCompleteJobTaskBatchList = Lists.newArrayList(jobTaskBatch);
        }

        boolean onlyTimeoutCheck = false;
        for (JobTaskBatch jobTaskBatch : notCompleteJobTaskBatchList) {
            prepare.setExecutionAt(jobTaskBatch.getExecutionAt());
            prepare.setTaskBatchId(jobTaskBatch.getId());
            prepare.setOnlyTimeoutCheck(onlyTimeoutCheck);
            for (JobPrepareHandler prepareHandler : prepareHandlers) {
                if (prepareHandler.matches(jobTaskBatch.getTaskBatchStatus())) {
                    prepareHandler.handle(prepare);
                    break;
                }
            }

            // 当存在大量待处理任务时，除了第一个任务需要执行阻塞策略，其他任务只做任务检查
            onlyTimeoutCheck = true;
        }
    }
}
