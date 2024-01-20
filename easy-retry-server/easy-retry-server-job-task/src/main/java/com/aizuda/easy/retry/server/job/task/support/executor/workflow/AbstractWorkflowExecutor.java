package com.aizuda.easy.retry.server.job.task.support.executor.workflow;

import akka.actor.ActorRef;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.aizuda.easy.retry.common.core.enums.*;
import com.aizuda.easy.retry.server.common.akka.ActorGenerator;
import com.aizuda.easy.retry.server.common.exception.EasyRetryServerException;
import com.aizuda.easy.retry.server.job.task.dto.WorkflowNodeTaskExecuteDTO;
import com.aizuda.easy.retry.server.job.task.support.WorkflowExecutor;
import com.aizuda.easy.retry.server.job.task.support.WorkflowTaskConverter;
import com.aizuda.easy.retry.server.job.task.support.generator.batch.JobTaskBatchGenerator;
import com.aizuda.easy.retry.server.job.task.support.generator.batch.JobTaskBatchGeneratorContext;
import com.aizuda.easy.retry.server.job.task.support.handler.DistributedLockHandler;
import com.aizuda.easy.retry.server.job.task.support.handler.WorkflowBatchHandler;
import com.aizuda.easy.retry.template.datasource.persistence.mapper.JobTaskBatchMapper;
import com.aizuda.easy.retry.template.datasource.persistence.mapper.JobTaskMapper;
import com.aizuda.easy.retry.template.datasource.persistence.po.JobTask;
import com.aizuda.easy.retry.template.datasource.persistence.po.JobTaskBatch;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.CollectionUtils;

import java.text.MessageFormat;
import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.aizuda.easy.retry.common.core.enums.JobOperationReasonEnum.WORKFLOW_SUCCESSOR_SKIP_EXECUTION;

/**
 * @author xiaowoniu
 * @date 2023-12-24 08:15:19
 * @since 2.6.0
 */
@Slf4j
public abstract class AbstractWorkflowExecutor implements WorkflowExecutor, InitializingBean {

    private static final String KEY = "workflow_execute_{0}_{1}";
    @Autowired
    private DistributedLockHandler distributedLockHandler;
    @Autowired
    private JobTaskBatchMapper jobTaskBatchMapper;
    @Autowired
    private JobTaskBatchGenerator jobTaskBatchGenerator;
    @Autowired
    protected WorkflowBatchHandler workflowBatchHandler;
    @Autowired
    private JobTaskMapper jobTaskMapper;
    @Autowired
    private TransactionTemplate transactionTemplate;

    @Override
    @Transactional
    public void execute(WorkflowExecutorContext context) {
        distributedLockHandler.lockWithDisposableAndRetry(
                () -> {

                    long total = 0;
                    // 条件节点存在并发问题，需要特殊处理
                    if (WorkflowNodeTypeEnum.DECISION.getType() == context.getNodeType()) {
                        List<JobTaskBatch> jobTaskBatches = jobTaskBatchMapper.selectList(new LambdaQueryWrapper<JobTaskBatch>()
                                .select(JobTaskBatch::getOperationReason)
                                .eq(JobTaskBatch::getWorkflowTaskBatchId, context.getWorkflowTaskBatchId())
                                .eq(JobTaskBatch::getWorkflowNodeId, context.getWorkflowNodeId())
                        );

                        if (!CollectionUtils.isEmpty(jobTaskBatches)) {
                            total = jobTaskBatches.size();
                            JobTaskBatch jobTaskBatch = jobTaskBatches.get(0);
                            if (WORKFLOW_SUCCESSOR_SKIP_EXECUTION.contains(jobTaskBatch.getOperationReason())) {
                                context.setEvaluationResult(Boolean.FALSE);
                            } else {
                                context.setEvaluationResult(Boolean.TRUE);
                            }
                        }

                    } else {
                        total = jobTaskBatchMapper.selectCount(new LambdaQueryWrapper<JobTaskBatch>()
                                .eq(JobTaskBatch::getWorkflowTaskBatchId, context.getWorkflowTaskBatchId())
                                .eq(JobTaskBatch::getWorkflowNodeId, context.getWorkflowNodeId())
                        );
                    }

                    if (total > 0) {
                        log.warn("任务节点[{}]已被执行，请勿重复执行", context.getWorkflowNodeId());
                        return;
                    }

                    transactionTemplate.execute(new TransactionCallbackWithoutResult() {
                        @Override
                        protected void doInTransactionWithoutResult(final TransactionStatus status) {

                            if (!preValidate(context)) {
                                return;
                            }
                            beforeExecute(context);

                            doExecute(context);

                            afterExecute(context);
                        }
                    });
                }, MessageFormat.format(KEY, context.getWorkflowTaskBatchId(), context.getWorkflowNodeId()),
                Duration.ofSeconds(5), Duration.ofSeconds(1), 3);

    }

    protected boolean preValidate(WorkflowExecutorContext context) {
        return doPreValidate(context);
    }

    protected abstract boolean doPreValidate(WorkflowExecutorContext context);

    protected abstract void afterExecute(WorkflowExecutorContext context);

    protected abstract void beforeExecute(WorkflowExecutorContext context);

    protected abstract void doExecute(WorkflowExecutorContext context);

    protected JobTaskBatch generateJobTaskBatch(WorkflowExecutorContext context) {
        JobTaskBatchGeneratorContext generatorContext = WorkflowTaskConverter.INSTANCE.toJobTaskBatchGeneratorContext(context);
        return jobTaskBatchGenerator.generateJobTaskBatch(generatorContext);
    }

    protected void workflowTaskExecutor(WorkflowExecutorContext context) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                try {
                    WorkflowNodeTaskExecuteDTO taskExecuteDTO = new WorkflowNodeTaskExecuteDTO();
                    taskExecuteDTO.setWorkflowTaskBatchId(context.getWorkflowTaskBatchId());
                    taskExecuteDTO.setTaskExecutorScene(context.getTaskExecutorScene());
                    taskExecuteDTO.setParentId(context.getWorkflowNodeId());
                    taskExecuteDTO.setTaskBatchId(context.getTaskBatchId());
                    ActorRef actorRef = ActorGenerator.workflowTaskExecutorActor();
                    actorRef.tell(taskExecuteDTO, actorRef);
                } catch (Exception e) {
                    log.error("工作流执行失败", e);
                }
            }
        });
    }

    protected JobTask generateJobTask(WorkflowExecutorContext context, JobTaskBatch jobTaskBatch) {
        // 生成执行任务实例
        JobTask jobTask = new JobTask();
        jobTask.setGroupName(context.getGroupName());
        jobTask.setNamespaceId(context.getNamespaceId());
        jobTask.setJobId(context.getJobId());
        jobTask.setClientInfo(StrUtil.EMPTY);
        jobTask.setTaskBatchId(jobTaskBatch.getId());
        jobTask.setArgsType(JobArgsTypeEnum.TEXT.getArgsType());
        jobTask.setArgsStr(Optional.ofNullable(context.getTaskResult()).orElse(StrUtil.EMPTY));
        jobTask.setTaskStatus(context.getJobTaskStatus());
        jobTask.setResultMessage(String.valueOf(context.getEvaluationResult()));
        Assert.isTrue(1 == jobTaskMapper.insert(jobTask), () -> new EasyRetryServerException("新增任务实例失败"));
        return jobTask;
    }

    @Override
    public void afterPropertiesSet() {
        WorkflowExecutorFactory.registerJobExecutor(getWorkflowNodeType(), this);
    }
}
