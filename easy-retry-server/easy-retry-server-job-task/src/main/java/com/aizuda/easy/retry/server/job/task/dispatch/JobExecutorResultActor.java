package com.aizuda.easy.retry.server.job.task.dispatch;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import cn.hutool.core.lang.Assert;
import com.aizuda.easy.retry.common.core.enums.JobTaskBatchStatusEnum;
import com.aizuda.easy.retry.common.core.util.JsonUtil;
import com.aizuda.easy.retry.server.common.akka.ActorGenerator;
import com.aizuda.easy.retry.server.common.cache.CacheGroupScanActor;
import com.aizuda.easy.retry.server.common.dto.ScanTask;
import com.aizuda.easy.retry.server.common.enums.TaskTypeEnum;
import com.aizuda.easy.retry.server.common.exception.EasyRetryServerException;
import com.aizuda.easy.retry.server.job.task.JobTaskConverter;
import com.aizuda.easy.retry.server.job.task.WaitStrategy;
import com.aizuda.easy.retry.server.job.task.dto.JobExecutorResultDTO;
import com.aizuda.easy.retry.server.job.task.dto.JobLogDTO;
import com.aizuda.easy.retry.server.job.task.dto.JobTaskPrepareDTO;
import com.aizuda.easy.retry.server.job.task.handler.helper.JobTaskBatchHelper;
import com.aizuda.easy.retry.server.job.task.strategy.WaitStrategies.WaitStrategyContext;
import com.aizuda.easy.retry.server.job.task.strategy.WaitStrategies.WaitStrategyEnum;
import com.aizuda.easy.retry.template.datasource.persistence.mapper.JobTaskMapper;
import com.aizuda.easy.retry.template.datasource.persistence.mapper.JobTaskBatchMapper;
import com.aizuda.easy.retry.template.datasource.persistence.po.JobTask;
import com.aizuda.easy.retry.template.datasource.persistence.po.JobTaskBatch;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * @author www.byteblogs.com
 * @date 2023-10-05 17:16:35
 * @since 2.4.0
 */
@Component(ActorGenerator.JOB_EXECUTOR_RESULT_ACTOR)
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Slf4j
public class JobExecutorResultActor extends AbstractActor {

    @Autowired
    private JobTaskMapper jobTaskMapper;
    @Autowired
    private TransactionTemplate transactionTemplate;
    @Autowired
    private JobTaskBatchHelper jobTaskBatchHelper;
    @Autowired
    private JobTaskBatchMapper jobTaskBatchMapper;

    @Override
    public Receive createReceive() {
        return receiveBuilder().match(JobExecutorResultDTO.class, result -> {
            log.info("更新任务状态. 参数:[{}]", JsonUtil.toJsonString(result));

            transactionTemplate.execute(new TransactionCallbackWithoutResult() {
                @Override
                protected void doInTransactionWithoutResult(final TransactionStatus status) {
                    JobTask jobTask = new JobTask();
                    jobTask.setExecuteStatus(result.getTaskStatus());
                    if (Objects.nonNull(result.getResult())) {
                        jobTask.setResultMessage(JsonUtil.toJsonString(result.getResult()));
                    }

                    Assert.isTrue(1 == jobTaskMapper.update(jobTask,
                            new LambdaUpdateWrapper<JobTask>().eq(JobTask::getId, result.getTaskId())),
                        ()-> new EasyRetryServerException("更新任务实例失败"));

                    // 更新批次上的状态
                    jobTaskBatchHelper.complete(result.getTaskBatchId());
                }
            });

            // TODO 60秒内的任务直接丢入时间轮中
            if (Integer.parseInt("30") <= 60) {
                if (jobTaskBatchMapper.selectCount(new LambdaQueryWrapper<JobTaskBatch>()
                        .eq(JobTaskBatch::getId, result.getTaskBatchId())
                    .in(JobTaskBatch::getTaskStatus, JobTaskBatchStatusEnum.NOT_COMPLETE)) <= 0) {
                    ActorRef scanActorRef = CacheGroupScanActor.get("DEFAULT_JOB_KEY", TaskTypeEnum.JOB);
                    ScanTask scanTask = new ScanTask();
                    scanTask.setBuckets(Sets.newHashSet(0));
                    scanTask.setSize(1);
                    scanTask.setStartId(result.getJobId());
                    scanActorRef.tell(scanTask, scanActorRef);
                }
            }

            JobLogDTO jobLogDTO = JobTaskConverter.INSTANCE.toJobLogDTO(result);
            jobLogDTO.setMessage(result.getMessage());
            jobLogDTO.setClientId(result.getClientId());
            jobLogDTO.setTaskId(result.getTaskId());
            ActorRef actorRef = ActorGenerator.jobLogActor();
            actorRef.tell(jobLogDTO, actorRef);

        }).build();
    }
}
